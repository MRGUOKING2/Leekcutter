package emu.grasscutter.scripts;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.def.MonsterData;
import emu.grasscutter.data.def.WorldLevelData;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneBlock;
import emu.grasscutter.scripts.data.SceneConfig;
import emu.grasscutter.scripts.data.SceneGadget;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.SceneInitConfig;
import emu.grasscutter.scripts.data.SceneMonster;
import emu.grasscutter.scripts.data.SceneRegion;
import emu.grasscutter.scripts.data.SceneSuite;
import emu.grasscutter.scripts.data.SceneTrigger;
import emu.grasscutter.scripts.data.SceneVar;
import emu.grasscutter.scripts.data.ScriptArgs;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class SceneScriptManager {
	private final Scene scene;
	private final ScriptLib scriptLib;
	private final LuaValue scriptLibLua;
	private final Map<String, Integer> variables;
	
	private Bindings bindings;
	private SceneConfig config;
	private List<SceneBlock> blocks;
	private boolean isInit;
	
	private final Int2ObjectOpenHashMap<Set<SceneTrigger>> triggers;
	private final Int2ObjectOpenHashMap<SceneRegion> regions;
	private SceneGroup currentGroup;
	private AtomicInteger monsterAlive;
	private AtomicInteger monsterTideCount;
	private int monsterSceneLimit;
	private ConcurrentLinkedQueue<Integer> monsterOrders;

	public SceneScriptManager(Scene scene) {
		this.scene = scene;
		this.scriptLib = new ScriptLib(this);
		this.scriptLibLua = CoerceJavaToLua.coerce(this.scriptLib);
		this.triggers = new Int2ObjectOpenHashMap<>();
		this.regions = new Int2ObjectOpenHashMap<>();
		this.variables = new HashMap<>();
		
		// TEMPORARY
		if (this.getScene().getId() < 10) {
			return;
		}
		
		// Create
		this.init();
	}
	
	public Scene getScene() {
		return scene;
	}

	public ScriptLib getScriptLib() {
		return scriptLib;
	}
	
	public LuaValue getScriptLibLua() {
		return scriptLibLua;
	}

	public Bindings getBindings() {
		return bindings;
	}

	public SceneConfig getConfig() {
		return config;
	}

	public SceneGroup getCurrentGroup() {
		return currentGroup;
	}

	public List<SceneBlock> getBlocks() {
		return blocks;
	}

	public Map<String, Integer> getVariables() {
		return variables;
	}

	public Set<SceneTrigger> getTriggersByEvent(int eventId) {
		return triggers.computeIfAbsent(eventId, e -> new HashSet<>());
	}
	
	public void registerTrigger(SceneTrigger trigger) {
		getTriggersByEvent(trigger.event).add(trigger);
	}
	
	public void deregisterTrigger(SceneTrigger trigger) {
		getTriggersByEvent(trigger.event).remove(trigger);
	}
	
	public SceneRegion getRegionById(int id) {
		return regions.get(id);
	}
	
	public void registerRegion(SceneRegion region) {
		regions.put(region.config_id, region);
	}
	
	public void deregisterRegion(SceneRegion region) {
		regions.remove(region.config_id);
	}
	
	// TODO optimize
	public SceneGroup getGroupById(int groupId) {
		for (SceneBlock block : this.getScene().getLoadedBlocks()) {
			for (SceneGroup group : block.groups) {
				if (group.id == groupId) {
					return group;
				}
			}
		}
		return null;
	}

	private void init() {
		// Get compiled script if cached
		CompiledScript cs = ScriptLoader.getScriptByPath(
			Grasscutter.getConfig().SCRIPTS_FOLDER + "Scene/" + getScene().getId() + "/scene" + getScene().getId() + "." + ScriptLoader.getScriptType());
		
		if (cs == null) {
			Grasscutter.getLogger().warn("No script found for scene " + getScene().getId());
			return;
		}
		
		// Create bindings
		bindings = ScriptLoader.getEngine().createBindings();
		
		// Set variables
		bindings.put("ScriptLib", getScriptLib());

		// Eval script
		try {
			cs.eval(getBindings());
			
			this.config = ScriptLoader.getSerializer().toObject(SceneConfig.class, bindings.get("scene_config"));
			
			// TODO optimize later
			// Create blocks
			List<Integer> blockIds = ScriptLoader.getSerializer().toList(Integer.class, bindings.get("blocks"));
			List<SceneBlock> blocks = ScriptLoader.getSerializer().toList(SceneBlock.class, bindings.get("block_rects"));
			
			for (int i = 0; i < blocks.size(); i++) {
				SceneBlock block = blocks.get(i);
				block.id = blockIds.get(i);
				
				loadBlockFromScript(block);
			}
			
			this.blocks = blocks;
		} catch (ScriptException e) {
			Grasscutter.getLogger().error("Error running script", e);
			return;
		}
		
		// TEMP
		this.isInit = true;
	}

	public boolean isInit() {
		return isInit;
	}
	
	private void loadBlockFromScript(SceneBlock block) {
		CompiledScript cs = ScriptLoader.getScriptByPath(
			Grasscutter.getConfig().SCRIPTS_FOLDER + "Scene/" + getScene().getId() + "/scene" + getScene().getId() + "_block" + block.id + "." + ScriptLoader.getScriptType());
	
		if (cs == null) {
			return;
		}
		
		// Eval script
		try {
			cs.eval(getBindings());
			
			// Set groups
			block.groups = ScriptLoader.getSerializer().toList(SceneGroup.class, bindings.get("groups"));
			block.groups.forEach(g -> g.block_id = block.id);
		} catch (ScriptException e) {
			Grasscutter.getLogger().error("Error loading block " + block.id + " in scene " + getScene().getId(), e);
		}
	}
	
	public void loadGroupFromScript(SceneGroup group) {
		// Set flag here so if there is no script, we dont call this function over and over again.
		group.setLoaded(true);
		
		CompiledScript cs = ScriptLoader.getScriptByPath(
			Grasscutter.getConfig().SCRIPTS_FOLDER + "Scene/" + getScene().getId() + "/scene" + getScene().getId() + "_group" + group.id + "." + ScriptLoader.getScriptType());
	
		if (cs == null) {
			return;
		}
		
		// Eval script
		try {
			cs.eval(getBindings());

			// Set
			group.monsters = ScriptLoader.getSerializer().toList(SceneMonster.class, bindings.get("monsters")).stream()
					.collect(Collectors.toMap(x -> x.config_id, y -> y));
			group.gadgets = ScriptLoader.getSerializer().toList(SceneGadget.class, bindings.get("gadgets"));
			group.triggers = ScriptLoader.getSerializer().toList(SceneTrigger.class, bindings.get("triggers"));
			group.suites = ScriptLoader.getSerializer().toList(SceneSuite.class, bindings.get("suites"));
			group.regions = ScriptLoader.getSerializer().toList(SceneRegion.class, bindings.get("regions"));
			group.init_config = ScriptLoader.getSerializer().toObject(SceneInitConfig.class, bindings.get("init_config"));
			
			// Add variables to suite
			List<SceneVar> variables = ScriptLoader.getSerializer().toList(SceneVar.class, bindings.get("variables"));
			variables.forEach(var -> this.getVariables().put(var.name, var.value));
			
			// Add monsters to suite TODO optimize
			Int2ObjectMap<Object> map = new Int2ObjectOpenHashMap<>();
			group.monsters.entrySet().forEach(m -> map.put(m.getValue().config_id, m));
			group.gadgets.forEach(m -> map.put(m.config_id, m));
			
			for (SceneSuite suite : group.suites) {
				suite.sceneMonsters = new ArrayList<>(suite.monsters.size());
				suite.monsters.forEach(id -> {
					Object objEntry = map.get(id.intValue());
					if (objEntry instanceof Map.Entry<?,?> monsterEntry) {
						Object monster = monsterEntry.getValue();
						if(monster instanceof SceneMonster sceneMonster){
							suite.sceneMonsters.add(sceneMonster);
						}
					}
				});

				suite.sceneGadgets = new ArrayList<>(suite.gadgets.size());
				for (int id : suite.gadgets) {
					try {
						SceneGadget gadget = (SceneGadget) map.get(id);
						if (gadget != null) {
							suite.sceneGadgets.add(gadget);
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
		} catch (ScriptException e) {
			Grasscutter.getLogger().error("Error loading group " + group.id + " in scene " + getScene().getId(), e);
		}
	}

	public void onTick() {
		checkRegions();
	}
	
	public void checkRegions() {
		if (this.regions.size() == 0) {
			return;
		}
		
		for (SceneRegion region : this.regions.values()) {
			getScene().getEntities().values()
				.stream()
				.filter(e -> e.getEntityType() <= 2 && region.contains(e.getPosition()))
				.forEach(region::addEntity);

			if (region.hasNewEntities()) {
				// This is not how it works, source_eid should be region entity id, but we dont have an entity for regions yet
				callEvent(EventType.EVENT_ENTER_REGION, new ScriptArgs(region.config_id).setSourceEntityId(region.config_id));
				
				region.resetNewEntities();
			}
		}
	}
	
	public void spawnGadgetsInGroup(SceneGroup group, int suiteIndex) {
		spawnGadgetsInGroup(group, group.getSuiteByIndex(suiteIndex));
	}
	
	public void spawnGadgetsInGroup(SceneGroup group) {
		spawnGadgetsInGroup(group, null);
	}
	
	public void spawnGadgetsInGroup(SceneGroup group, SceneSuite suite) {
		List<SceneGadget> gadgets = group.gadgets;
		
		if (suite != null) {
			gadgets = suite.sceneGadgets;
		}
		
		for (SceneGadget g : gadgets) {
			EntityGadget entity = new EntityGadget(getScene(), g.gadget_id, g.pos);
			
			if (entity.getGadgetData() == null) continue;
			
			entity.setBlockId(group.block_id);
			entity.setConfigId(g.config_id);
			entity.setGroupId(group.id);
			entity.getRotation().set(g.rot);
			entity.setState(g.state);
			
			getScene().addEntity(entity);
			this.callEvent(EventType.EVENT_GADGET_CREATE, new ScriptArgs(entity.getConfigId()));
		}
	}
	
	public void spawnMonstersInGroup(SceneGroup group, int suiteIndex) {
		var suite = group.getSuiteByIndex(suiteIndex);
		if(suite == null){
			return;
		}
		if(suite.sceneMonsters.size() > 0){
			this.currentGroup = group;
			this.monsterSceneLimit = 0;
			suite.sceneMonsters.forEach(mob -> spawnMonstersInGroup(group, mob));
		}
	}
	
	public void spawnMonstersInGroup(SceneGroup group) {
		this.currentGroup = group;
		this.monsterSceneLimit = 0;
		group.monsters.values().forEach(mob -> spawnMonstersInGroup(group, mob));
	}
	public void spawnMonstersInGroup(SceneGroup group,Integer[] ordersConfigId, int tideCount, int sceneLimit) {
		this.currentGroup = group;
		this.monsterSceneLimit = sceneLimit;
		this.monsterTideCount = new AtomicInteger(tideCount);
		this.monsterAlive = new AtomicInteger(0);
		this.monsterOrders = new ConcurrentLinkedQueue<>(List.of(ordersConfigId));

		// add the last turn
		group.monsters.keySet().stream()
				.filter(i -> !this.monsterOrders.contains(i))
				.forEach(this.monsterOrders::add);
		for (int i = 0; i < sceneLimit; i++) {
			spawnMonstersInGroup(group, group.monsters.get(this.monsterOrders.poll()));
		}
	}
	public void spawnMonstersInGroup(SceneGroup group, SceneMonster monster) {
		if(monster == null){
			return;
		}
		if(this.monsterSceneLimit > 0){
			this.monsterTideCount.decrementAndGet();
			this.monsterAlive.incrementAndGet();
		}

		MonsterData data = GameData.getMonsterDataMap().get(monster.monster_id);

		if (data == null) {
			return;
		}

		// Calculate level
		int level = monster.level;

		if (getScene().getDungeonData() != null) {
			level = getScene().getDungeonData().getShowLevel();
		} else if (getScene().getWorld().getWorldLevel() > 0) {
			WorldLevelData worldLevelData = GameData.getWorldLevelDataMap().get(getScene().getWorld().getWorldLevel());

			if (worldLevelData != null) {
				level = worldLevelData.getMonsterLevel();
			}
		}

		// Spawn mob
		EntityMonster entity = new EntityMonster(getScene(), data, monster.pos, level);
		entity.getRotation().set(monster.rot);
		entity.setGroupId(group.id);
		entity.setConfigId(monster.config_id);

		getScene().addEntity(entity);

		callEvent(EventType.EVENT_ANY_MONSTER_LIVE, new ScriptArgs(entity.getConfigId()));
	}

	public void onMonsterDie(){
		if(this.monsterSceneLimit <= 0){
			return;
		}
		if(this.monsterAlive.decrementAndGet() >= this.monsterSceneLimit) {
			// maybe not happen
			return;
		}
		if(this.monsterTideCount.get() > 0){
			// add more
			spawnMonstersInGroup(this.currentGroup, this.currentGroup.monsters.get(this.monsterOrders.poll()));
		}else if(this.monsterAlive.get() == 0){
			// spawn the last turn of monsters
			//callEvent(EventType.EVENT_MONSTER_TIDE_DIE, new ScriptArgs());
			while(!this.monsterOrders.isEmpty()){
				spawnMonstersInGroup(this.currentGroup, this.currentGroup.monsters.get(this.monsterOrders.poll()));
			}
		}
	}
	// Events
	
	public void callEvent(int eventType, ScriptArgs params) {
		for (SceneTrigger trigger : this.getTriggersByEvent(eventType)) {
			LuaValue condition = null;
			
			if (trigger.condition != null && !trigger.condition.isEmpty()) {
				condition = (LuaValue) this.getBindings().get(trigger.condition);
			}
			
			LuaValue ret = LuaValue.TRUE;
			
			if (condition != null) {
				LuaValue args = LuaValue.NIL;
				
				if (params != null) {
					args = CoerceJavaToLua.coerce(params);
				}
				
				ret = condition.call(this.getScriptLibLua(), args);
			}
			
			if (ret.checkboolean() == true) {
				LuaValue action = (LuaValue) this.getBindings().get(trigger.action);
				action.call(this.getScriptLibLua(), LuaValue.NIL);
			}
		}
	}

//	public LuaValue safetyCall(){
//
//	}
}
