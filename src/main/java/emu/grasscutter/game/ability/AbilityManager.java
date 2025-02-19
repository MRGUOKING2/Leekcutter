package emu.grasscutter.game.ability;

import com.google.protobuf.InvalidProtocolBufferException;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.custom.AbilityModifier;
import emu.grasscutter.data.custom.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.data.custom.AbilityModifier.AbilityModifierActionType;
import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.data.custom.AbilityModifierEntry;
import emu.grasscutter.game.entity.EntityItem;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.AbilityActionGenerateElemBallOuterClass.AbilityActionGenerateElemBall;
import emu.grasscutter.net.proto.AbilityInvokeArgumentOuterClass.AbilityInvokeArgument;
import emu.grasscutter.net.proto.AbilityInvokeEntryHeadOuterClass.AbilityInvokeEntryHead;
import emu.grasscutter.net.proto.AbilityInvokeEntryOuterClass.AbilityInvokeEntry;
import emu.grasscutter.net.proto.AbilityMetaModifierChangeOuterClass.AbilityMetaModifierChange;
import emu.grasscutter.net.proto.AbilityMetaReInitOverrideMapOuterClass.AbilityMetaReInitOverrideMap;
import emu.grasscutter.net.proto.AbilityScalarTypeOuterClass.AbilityScalarType;
import emu.grasscutter.net.proto.AbilityScalarValueEntryOuterClass.AbilityScalarValueEntry;
import emu.grasscutter.net.proto.ModifierActionOuterClass.ModifierAction;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.Utils;

public class AbilityManager {
	private Player player;
	
	public AbilityManager(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return this.player;
	}

	public void onAbilityInvoke(AbilityInvokeEntry invoke) throws Exception {
		//System.out.println(invoke.getArgumentType() + " (" + invoke.getArgumentTypeValue() + "): " + Utils.bytesToHex(invoke.toByteArray()));
		switch (invoke.getArgumentType()) {
			case ABILITY_META_OVERRIDE_PARAM:
				handleOverrideParam(invoke);
				break;
			case ABILITY_META_REINIT_OVERRIDEMAP:
				handleReinitOverrideMap(invoke);
				break;
			case ABILITY_META_MODIFIER_CHANGE:
				handleModifierChange(invoke);
				break;
			case ABILITY_MIXIN_COST_STAMINA:
				handleMixinCostStamina(invoke);
				break;
			case ABILITY_ACTION_GENERATE_ELEM_BALL:
				handleGenerateElemBall(invoke);
				break;
			default:
				break;
		}
	}

	private void handleOverrideParam(AbilityInvokeEntry invoke) throws Exception {
		GameEntity entity = player.getScene().getEntityById(invoke.getEntityId());
		
		if (entity == null) {
			return;
		}
		
		AbilityScalarValueEntry entry = AbilityScalarValueEntry.parseFrom(invoke.getAbilityData());
		
		entity.getMetaOverrideMap().put(entry.getKey().getStr(), entry.getFloatValue());
	}

	private void handleReinitOverrideMap(AbilityInvokeEntry invoke) throws Exception {
		GameEntity entity = player.getScene().getEntityById(invoke.getEntityId());
		
		if (entity == null) {
			return;
		}
		
		AbilityMetaReInitOverrideMap map = AbilityMetaReInitOverrideMap.parseFrom(invoke.getAbilityData());
		
		for (AbilityScalarValueEntry entry : map.getOverrideMapList()) {
			entity.getMetaOverrideMap().put(entry.getKey().getStr(), entry.getFloatValue());
		}
	}
	
	private void handleModifierChange(AbilityInvokeEntry invoke) throws Exception {
		GameEntity target = player.getScene().getEntityById(invoke.getEntityId());
		if (target == null) {
			return;
		}
		
		AbilityInvokeEntryHead head = invoke.getHead();
		if (head == null) {
			return;
		}
		
		AbilityMetaModifierChange data = AbilityMetaModifierChange.parseFrom(invoke.getAbilityData());
		if (data == null) {
			return;
		}
		
		GameEntity sourceEntity = player.getScene().getEntityById(data.getApplyEntityId());
		if (sourceEntity == null) {
			return;
		}
		
		// This is not how it works but we will keep it for now since healing abilities dont work properly anyways
		if (data.getAction() == ModifierAction.ADDED && data.getParentAbilityName() != null) {
			// Handle add modifier here
			String modifierString = data.getParentAbilityName().getStr();
			AbilityModifierEntry modifier = GameData.getAbilityModifiers().get(modifierString);
			
			if (modifier != null && modifier.getOnAdded().size() > 0) {
				for (AbilityModifierAction action : modifier.getOnAdded()) {
					invokeAction(action, target, sourceEntity);
				}
			}
			
			// Add to meta modifier list
			target.getMetaModifiers().put(head.getInstancedModifierId(), modifierString);
		} else if (data.getAction() == ModifierAction.REMOVED) {
			String modifierString = target.getMetaModifiers().get(head.getInstancedModifierId());
			
			if (modifierString != null) {
				// Get modifier and call on remove event
				AbilityModifierEntry modifier = GameData.getAbilityModifiers().get(modifierString);
				
				if (modifier != null && modifier.getOnRemoved().size() > 0) {
					for (AbilityModifierAction action : modifier.getOnRemoved()) {
						invokeAction(action, target, sourceEntity);
					}
				}
				
				// Remove from meta modifiers
				target.getMetaModifiers().remove(head.getInstancedModifierId());
			}
		}
	}
	
	private void handleMixinCostStamina(AbilityInvokeEntry invoke) {
		// Not the right way of doing this
		if (Grasscutter.getConfig().OpenStamina) {
			// getPlayer().getStaminaManager().updateStamina(getPlayer().getSession(), -450);
			// TODO
			// set flag in stamina/movement manager that specifies the player is currently using an alternate sprint
		}
	}
	
	private void handleGenerateElemBall(AbilityInvokeEntry invoke) throws InvalidProtocolBufferException {
		AbilityActionGenerateElemBall action = AbilityActionGenerateElemBall.parseFrom(invoke.getAbilityData());
		if (action == null) {
			return;
		}
		
		ItemData itemData = GameData.getItemDataMap().get(2024);
		if (itemData == null) {
			return; // Should never happen
		}
		
		EntityItem energyBall = new EntityItem(getPlayer().getScene(), getPlayer(), itemData, new Position(action.getPos()), 1);
		energyBall.getRotation().set(action.getRot());
		
		getPlayer().getScene().addEntity(energyBall);
	}
	
	private void invokeAction(AbilityModifierAction action, GameEntity target, GameEntity sourceEntity) {
		switch (action.type) {
			case HealHP -> {
				if (action.amount == null) {
					return;
				}
				
				float healAmount = 0;
				
				if (action.amount.isDynamic && action.amount.dynamicKey != null) {
					healAmount = sourceEntity.getMetaOverrideMap().getOrDefault(action.amount.dynamicKey, 0f);
				}
				
				if (healAmount > 0) {
					target.heal(healAmount);
				}
			}
			case LoseHP -> {
				if (action.amountByTargetCurrentHPRatio == null) {
					return;
				}
				
				float damageAmount = 0;
				
				if (action.amount.isDynamic && action.amount.dynamicKey != null) {
					damageAmount = sourceEntity.getMetaOverrideMap().getOrDefault(action.amount.dynamicKey, 0f);
				}
				
				if (damageAmount > 0) {
					target.damage(damageAmount);
				}
			}
		}
	}
}

