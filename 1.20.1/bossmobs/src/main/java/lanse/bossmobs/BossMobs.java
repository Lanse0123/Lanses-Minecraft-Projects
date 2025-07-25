package lanse.bossmobs;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

public class BossMobs implements ModInitializer {

	public static int tickCount = 0;
	public static boolean AllMobsAreBosses = false;
	private static final ArrayList<Integer> bossIdList = new ArrayList<>();
	private static final ArrayList<Integer> minionIdList = new ArrayList<>();
	private static final Map<Integer, Integer> bossTickMap = new HashMap<>();
	public static boolean bossMobsIsOn = false;
	public static boolean bossDamageDefined = false;

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> BossMobsCommands.register(dispatcher));
		new DeathListener();
		new SpawnListener();
	}

	private void onServerTick(MinecraftServer server) {
		if (bossMobsIsOn) {
			if (tickCount > 100000){
				tickCount = 0;
			}

			updateBossTicks();
			tickBossMobs(server);
			tickCount++;
		}
	}

	private void tickBossMobs(MinecraftServer server) {
		if (!bossIdList.isEmpty()) {
			Collection<ServerWorld> worlds = (Collection<ServerWorld>) server.getWorlds();

			updateBossIdList(server);
			updateMinionIDList(server);

			// Iterate over a copy of the bossIdList to prevent ConcurrentModificationException... (IT WAS A PAIN)
			List<Integer> bossIdListCopy = new ArrayList<>(bossIdList);
			List<Integer> minionIdListCopy = new ArrayList<>(minionIdList);

			for (ServerWorld world : worlds) {
				for (int id : bossIdListCopy) {
					AttackHandler.HandleAttack(id, world, server);
				}
				for (int id : minionIdListCopy){
					AttackHandler.HandleMinion(id, world);
				}
			}
		}
	}

	public static void addId(int id){
		bossIdList.add(id);
	}

	public static void removeId(int id){
		if (bossIdList.isEmpty()){
			return;
		}
		for (int i = 0; i < bossIdList.size(); i++){
			if (bossIdList.get(i) == id){
				bossIdList.remove(i);
				i--;
			}
		}
	}
	public static boolean hasId(int id){
        return bossIdList.contains(id);
    }

	public static void updateBossIdList(MinecraftServer server) {

		List<Integer> bossIdListCopy = new ArrayList<>(bossIdList);
		List<Integer> bossIdNewList = new ArrayList<>();

		for (int bossID : bossIdListCopy) {
			for (ServerWorld world : server.getWorlds()) {
				Entity entity = world.getEntityById(bossID);

				if (entity != null && !bossIdNewList.contains(bossID)) {
					bossIdNewList.add(bossID);
				}
			}
		}
		if (bossIdList != bossIdNewList) {
			bossIdList.clear();
			bossIdList.addAll(bossIdNewList);
		}
	}

	public static void updateBossTicks() {
        bossTickMap.replaceAll((k, v) -> v + 1);
	}

	public static int getBossTickCount(int bossId) {
		return bossTickMap.getOrDefault(bossId, 0);
	}

	public static void resetBossTickCount(int bossId) {
		if (bossTickMap.containsKey(bossId)) {
			bossTickMap.put(bossId, 0);
		}
	}

	public static void addBossCounter(int bossId) {
		if (!bossTickMap.containsKey(bossId)) {
			bossTickMap.put(bossId, 0);
		}
	}

	public static void removeBossCounter(int bossID) {
		resetBossTickCount(bossID);
		bossTickMap.remove(bossID);
	}

	public static void addMinionID(int ID){
		minionIdList.add(ID);
	}

	public static void removeMinionID(int ID){
		{
			if (minionIdList.isEmpty()){
				return;
			}
			for (int i = 0; i < minionIdList.size(); i++){
				if (minionIdList.get(i) == ID){
					minionIdList.remove(i);
					i--;
				}
			}
		}
	}

	public static boolean hasMinionID(int ID){return minionIdList.contains(ID);}

	public static void updateMinionIDList(MinecraftServer server) {

		List<Integer> minionIdListCopy = new ArrayList<>(minionIdList);
		List<Integer> minionIdNewList = new ArrayList<>();

		for (int ID : minionIdListCopy) {
			for (ServerWorld world : server.getWorlds()) {
				Entity entity = world.getEntityById(ID);

				if (entity != null && !minionIdNewList.contains(ID)) {
					minionIdNewList.add(ID);
				}
			}
		}
		if (minionIdList != minionIdNewList) {
			minionIdList.clear();
			minionIdList.addAll(minionIdNewList);
		}
	}
}