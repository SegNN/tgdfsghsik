package ua.fichadev.fichacases.nms;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.Vector3f;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class CustomArmorStand extends EntityArmorStand {
   protected static List<CustomArmorStand> handlerList = new ArrayList<>();
   private boolean isSpawned;

   public CustomArmorStand(Location location) {
      super(EntityTypes.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      float yaw = location.getYaw();
      float pitch = location.getPitch();
      this.setPositionRotation(x, y, z, yaw, pitch);
      this.setNoGravity(true);
      this.setInvisible(true);
      this.setOnGround(true);
      this.collides = false;
      handlerList.add(this);
   }

   private void addToWorld() {
      PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(this);
      this.sendPacket(spawnPacket);
   }

   public void spawn(Location location) {
      this.addToWorld();
      this.sendMetadata();
      this.pureTeleport(location);
      this.isSpawned = true;
   }

   private void pureTeleport(Location location) {
      this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      this.setHeadRotation(location.getYaw());
      PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(this);
      this.sendPacket(teleportPacket);
   }

   public void teleport(Location location) {
      this.pureTeleport(location);
   }

   public void setCustomName(String name) {
      this.setCustomName(CraftChatMessage.fromStringOrNull(name));
      this.setCustomNameVisible(true);
      this.sendMetadata();
   }

   public void setNameVisible(boolean visible) {
      this.setCustomNameVisible(visible);
      this.sendMetadata();
   }

   public Location getLocation() {
      return new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
   }

   public void setHelmet(ItemStack item) {
      net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
      this.setSlot(EnumItemSlot.HEAD, nmsItem, false);
      PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(this.getId(), List.of(Pair.of(EnumItemSlot.HEAD, nmsItem)));
      this.sendPacket(equipmentPacket);
   }

   public ItemStack getHelmet() {
      return CraftItemStack.asBukkitCopy(this.getEquipment(EnumItemSlot.HEAD));
   }

   public void setHeadPose(EulerAngle angle) {
      this.setHeadPose(this.toNMS(angle));
      PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(this.getId(), this.datawatcher, true);
      this.sendPacket(metadataPacket);
   }

   public void destroy() {
      this.isSpawned = false;
      PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(new int[]{this.getId()});
      this.sendPacket(destroyPacket);
   }

   public void remove() {
      this.destroy();
      handlerList.remove(this);
   }

   public boolean isSpawned() {
      return this.isSpawned;
   }

   public void setMarker(boolean marker) {
      super.setMarker(marker);
      this.sendMetadata();
   }

   public void setBaby(boolean baby) {
      this.setSmall(baby);
      this.sendMetadata();
   }

   private void sendMetadata() {
      PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(this.getId(), this.datawatcher, true);
      this.sendPacket(metadataPacket);
   }

   private void sendPacket(Packet<?> packet) {
      this.world.getPlayers().forEach(entityHuman -> ((EntityPlayer)entityHuman).playerConnection.sendPacket(packet));
   }

   private Vector3f toNMS(EulerAngle old) {
      return new Vector3f((float)Math.toDegrees(old.getX()), (float)Math.toDegrees(old.getY()), (float)Math.toDegrees(old.getZ()));
   }

   protected void spawnFor(Player player) {
      PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(this);
      PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(this.getId(), this.datawatcher, true);
      PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(
         this.getId(), List.of(Pair.of(EnumItemSlot.HEAD, this.getEquipment(EnumItemSlot.HEAD)))
      );
      PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(this);
      PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
      connection.sendPacket(spawnPacket);
      connection.sendPacket(metadataPacket);
      connection.sendPacket(equipmentPacket);
      connection.sendPacket(teleportPacket);
   }
}
