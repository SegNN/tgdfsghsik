package org.dimasik.mantiitems.particles;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public enum Particles {
   EXPLOSION_NORMAL("explode", 0, -1, Particles.ParticleProperty.DIRECTIONAL),
   EXPLOSION_LARGE("largeexplode", 1, -1),
   EXPLOSION_HUGE("hugeexplosion", 2, -1),
   FIREWORKS_SPARK("fireworksSpark", 3, -1, Particles.ParticleProperty.DIRECTIONAL),
   WATER_BUBBLE("bubble", 4, -1, Particles.ParticleProperty.DIRECTIONAL, Particles.ParticleProperty.REQUIRES_WATER),
   WATER_SPLASH("splash", 5, -1, Particles.ParticleProperty.DIRECTIONAL),
   WATER_WAKE("wake", 6, 7, Particles.ParticleProperty.DIRECTIONAL),
   SUSPENDED("suspended", 7, -1, Particles.ParticleProperty.REQUIRES_WATER),
   SUSPENDED_DEPTH("depthSuspend", 8, -1, Particles.ParticleProperty.DIRECTIONAL),
   CRIT("crit", 9, -1, Particles.ParticleProperty.DIRECTIONAL),
   CRIT_MAGIC("magicCrit", 10, -1, Particles.ParticleProperty.DIRECTIONAL),
   SMOKE_NORMAL("smoke", 11, -1, Particles.ParticleProperty.DIRECTIONAL),
   SMOKE_LARGE("largesmoke", 12, -1, Particles.ParticleProperty.DIRECTIONAL),
   SPELL("spell", 13, -1),
   SPELL_INSTANT("instantSpell", 14, -1),
   SPELL_MOB("mobSpell", 15, -1, Particles.ParticleProperty.COLORABLE),
   SPELL_MOB_AMBIENT("mobSpellAmbient", 16, -1, Particles.ParticleProperty.COLORABLE),
   SPELL_WITCH("witchMagic", 17, -1),
   DRIP_WATER("dripWater", 18, -1),
   DRIP_LAVA("dripLava", 19, -1),
   VILLAGER_ANGRY("angryVillager", 20, -1),
   VILLAGER_HAPPY("happyVillager", 21, -1, Particles.ParticleProperty.DIRECTIONAL),
   TOWN_AURA("townaura", 22, -1, Particles.ParticleProperty.DIRECTIONAL),
   NOTE("note", 23, -1, Particles.ParticleProperty.COLORABLE),
   PORTAL("portal", 24, -1, Particles.ParticleProperty.DIRECTIONAL),
   ENCHANTMENT_TABLE("enchantmenttable", 25, -1, Particles.ParticleProperty.DIRECTIONAL),
   FLAME("flame", 26, -1, Particles.ParticleProperty.DIRECTIONAL),
   LAVA("lava", 27, -1),
   CLOUD("cloud", 29, -1, Particles.ParticleProperty.DIRECTIONAL),
   REDSTONE("reddust", 30, -1, Particles.ParticleProperty.COLORABLE, Particles.ParticleProperty.DIRECTIONAL),
   SNOWBALL("snowballpoof", 31, -1),
   SNOW_SHOVEL("snowshovel", 32, -1, Particles.ParticleProperty.DIRECTIONAL),
   SLIME("slime", 33, -1),
   HEART("heart", 34, -1),
   BARRIER("barrier", 35, 8),
   ITEM_CRACK("iconcrack", 36, -1, Particles.ParticleProperty.DIRECTIONAL, Particles.ParticleProperty.REQUIRES_DATA),
   BLOCK_CRACK("blockcrack", 37, -1, Particles.ParticleProperty.REQUIRES_DATA),
   BLOCK_DUST("blockdust", 38, 7, Particles.ParticleProperty.DIRECTIONAL, Particles.ParticleProperty.REQUIRES_DATA),
   WATER_DROP("droplet", 39, 8),
   DRAGON_BREATH("dragon_breath", 40, -1, Particles.ParticleProperty.DIRECTIONAL),
   MOB_APPEARANCE("mobappearance", 41, 8);

   private static final Map<String, Particles> NAME_MAP = new HashMap<>();
   private static final Map<Integer, Particles> ID_MAP = new HashMap<>();
   private final String name;
   private final int id;
   private final int requiredVersion;
   private final List<Particles.ParticleProperty> properties;

   private Particles(String name, int id, int requiredVersion, Particles.ParticleProperty... properties) {
      this.name = name;
      this.id = id;
      this.requiredVersion = requiredVersion;
      this.properties = Arrays.asList(properties);
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }

   public int getRequiredVersion() {
      return this.requiredVersion;
   }

   public boolean hasProperty(Particles.ParticleProperty property) {
      return this.properties.contains(property);
   }

   public boolean isSupported() {
      return this.requiredVersion == -1 || Particles.ParticlePacket.getVersion() >= this.requiredVersion;
   }

   public static Particles fromName(String name) {
      for (Entry<String, Particles> entry : NAME_MAP.entrySet()) {
         if (entry.getKey().equalsIgnoreCase(name)) {
            return entry.getValue();
         }
      }

      return null;
   }

   public static Particles fromId(int id) {
      for (Entry<Integer, Particles> entry : ID_MAP.entrySet()) {
         if (entry.getKey() == id) {
            return entry.getValue();
         }
      }

      return null;
   }

   private static boolean isLongDistance(Location location, List<Player> players) {
      String world = location.getWorld().getName();

      for (Player player : players) {
         Location playerLocation = player.getLocation();
         if (world.equals(playerLocation.getWorld().getName()) && !(playerLocation.distanceSquared(location) < 65536.0)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isDataCorrect(Particles effect, Particles.ParticleData data) {
      return (effect == BLOCK_CRACK || effect == BLOCK_DUST) && data instanceof Particles.BlockData
         || effect == ITEM_CRACK && data instanceof Particles.ItemData;
   }

   private static boolean isColorCorrect(Particles effect, Particles.ParticleColor color) {
      return (effect == SPELL_MOB || effect == SPELL_MOB_AMBIENT || effect == REDSTONE) && color instanceof Particles.OrdinaryColor
         || effect == NOTE && color instanceof Particles.NoteColor;
   }

   public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) throws Particles.ParticleVersionException, Particles.ParticleDataException, IllegalArgumentException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect requires additional data");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_WATER)) {
         throw new IllegalArgumentException("There is no water at the center location");
      } else {
         new Particles.ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, range > 256.0, null).sendTo(center, range);
      }
   }

   public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, List<Player> players) throws Particles.ParticleVersionException, Particles.ParticleDataException, IllegalArgumentException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect requires additional data");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_WATER)) {
         throw new IllegalArgumentException("There is no water at the center location");
      } else {
         new Particles.ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, isLongDistance(center, players), null).sendTo(center, players);
      }
   }

   public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, Player... players) throws Particles.ParticleVersionException, Particles.ParticleDataException, IllegalArgumentException {
      this.display(offsetX, offsetY, offsetZ, speed, amount, center, Arrays.asList(players));
   }

   public void display(Vector direction, float speed, Location center, double range) throws Particles.ParticleVersionException, Particles.ParticleDataException, IllegalArgumentException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect requires additional data");
      } else if (!this.hasProperty(Particles.ParticleProperty.DIRECTIONAL)) {
         throw new IllegalArgumentException("This particle effect is not directional");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_WATER)) {
         throw new IllegalArgumentException("There is no water at the center location");
      } else {
         new Particles.ParticlePacket(this, direction, speed, range > 256.0, null).sendTo(center, range);
      }
   }

   public void display(Vector direction, float speed, Location center, List<Player> players) throws Particles.ParticleVersionException, Particles.ParticleDataException, IllegalArgumentException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect requires additional data");
      } else if (!this.hasProperty(Particles.ParticleProperty.DIRECTIONAL)) {
         throw new IllegalArgumentException("This particle effect is not directional");
      } else if (this.hasProperty(Particles.ParticleProperty.REQUIRES_WATER)) {
         throw new IllegalArgumentException("There is no water at the center location");
      } else {
         new Particles.ParticlePacket(this, direction, speed, isLongDistance(center, players), null).sendTo(center, players);
      }
   }

   public void display(Vector direction, float speed, Location center, Player... players) throws Particles.ParticleVersionException, Particles.ParticleDataException, IllegalArgumentException {
      this.display(direction, speed, center, Arrays.asList(players));
   }

   public void display(Particles.ParticleColor color, Location center, double range) throws Particles.ParticleVersionException, Particles.ParticleColorException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (!this.hasProperty(Particles.ParticleProperty.COLORABLE)) {
         throw new Particles.ParticleColorException("This particle effect is not colorable");
      } else if (!isColorCorrect(this, color)) {
         throw new Particles.ParticleColorException("The particle color type is incorrect");
      } else {
         new Particles.ParticlePacket(this, color, range > 256.0).sendTo(center, range);
      }
   }

   public void display(Particles.ParticleColor color, Location center, List<Player> players) throws Particles.ParticleVersionException, Particles.ParticleColorException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (!this.hasProperty(Particles.ParticleProperty.COLORABLE)) {
         throw new Particles.ParticleColorException("This particle effect is not colorable");
      } else if (!isColorCorrect(this, color)) {
         throw new Particles.ParticleColorException("The particle color type is incorrect");
      } else {
         new Particles.ParticlePacket(this, color, isLongDistance(center, players)).sendTo(center, players);
      }
   }

   public void display(Particles.ParticleColor color, Location center, Player... players) throws Particles.ParticleVersionException, Particles.ParticleColorException {
      this.display(color, center, Arrays.asList(players));
   }

   public void display(Particles.ParticleData data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) throws Particles.ParticleVersionException, Particles.ParticleDataException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (!this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect does not require additional data");
      } else if (!isDataCorrect(this, data)) {
         throw new Particles.ParticleDataException("The particle data type is incorrect");
      } else {
         new Particles.ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, range > 256.0, data).sendTo(center, range);
      }
   }

   public void display(Particles.ParticleData data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, List<Player> players) throws Particles.ParticleVersionException, Particles.ParticleDataException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (!this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect does not require additional data");
      } else if (!isDataCorrect(this, data)) {
         throw new Particles.ParticleDataException("The particle data type is incorrect");
      } else {
         new Particles.ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, isLongDistance(center, players), data).sendTo(center, players);
      }
   }

   public void display(Particles.ParticleData data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, Player... players) throws Particles.ParticleVersionException, Particles.ParticleDataException {
      this.display(data, offsetX, offsetY, offsetZ, speed, amount, center, Arrays.asList(players));
   }

   public void display(Particles.ParticleData data, Vector direction, float speed, Location center, double range) throws Particles.ParticleVersionException, Particles.ParticleDataException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (!this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect does not require additional data");
      } else if (!isDataCorrect(this, data)) {
         throw new Particles.ParticleDataException("The particle data type is incorrect");
      } else {
         new Particles.ParticlePacket(this, direction, speed, range > 256.0, data).sendTo(center, range);
      }
   }

   public void display(Particles.ParticleData data, Vector direction, float speed, Location center, List<Player> players) throws Particles.ParticleVersionException, Particles.ParticleDataException {
      if (!this.isSupported()) {
         throw new Particles.ParticleVersionException("This particle effect is not supported by your server version");
      } else if (!this.hasProperty(Particles.ParticleProperty.REQUIRES_DATA)) {
         throw new Particles.ParticleDataException("This particle effect does not require additional data");
      } else if (!isDataCorrect(this, data)) {
         throw new Particles.ParticleDataException("The particle data type is incorrect");
      } else {
         new Particles.ParticlePacket(this, direction, speed, isLongDistance(center, players), data).sendTo(center, players);
      }
   }

   public void display(Particles.ParticleData data, Vector direction, float speed, Location center, Player... players) throws Particles.ParticleVersionException, Particles.ParticleDataException {
      this.display(data, direction, speed, center, Arrays.asList(players));
   }

   static {
      for (Particles effect : values()) {
         NAME_MAP.put(effect.name, effect);
         ID_MAP.put(effect.id, effect);
      }
   }

   public static final class BlockData extends Particles.ParticleData {
      public BlockData(Material material, byte data) throws IllegalArgumentException {
         super(material, data);
         if (!material.isBlock()) {
            throw new IllegalArgumentException("The material is not a block");
         }
      }
   }

   public static final class ItemData extends Particles.ParticleData {
      public ItemData(Material material, byte data) {
         super(material, data);
      }
   }

   public static final class NoteColor extends Particles.ParticleColor {
      private final int note;

      public NoteColor(int note) throws IllegalArgumentException {
         if (note < 0) {
            throw new IllegalArgumentException("The note value is lower than 0");
         } else if (note > 24) {
            throw new IllegalArgumentException("The note value is higher than 24");
         } else {
            this.note = note;
         }
      }

      @Override
      public float getValueX() {
         return this.note / 24.0F;
      }

      @Override
      public float getValueY() {
         return 0.0F;
      }

      @Override
      public float getValueZ() {
         return 0.0F;
      }
   }

   public static final class OrdinaryColor extends Particles.ParticleColor {
      private final int red;
      private final int green;
      private final int blue;

      public OrdinaryColor(int red, int green, int blue) throws IllegalArgumentException {
         this.red = Math.min(255, red);
         this.green = Math.min(255, green);
         this.blue = Math.min(255, blue);
      }

      public OrdinaryColor(Color color) {
         this(color.getRed(), color.getGreen(), color.getBlue());
      }

      public int getRed() {
         return this.red;
      }

      public int getGreen() {
         return this.green;
      }

      public int getBlue() {
         return this.blue;
      }

      @Override
      public float getValueX() {
         return this.red / 255.0F;
      }

      @Override
      public float getValueY() {
         return this.green / 255.0F;
      }

      @Override
      public float getValueZ() {
         return this.blue / 255.0F;
      }
   }

   public abstract static class ParticleColor {
      public ParticleColor() {
      }

      public abstract float getValueX();

      public abstract float getValueY();

      public abstract float getValueZ();
   }

   private static final class ParticleColorException extends RuntimeException {
      private static final long serialVersionUID = 3203085387160737484L;

      public ParticleColorException(String message) {
         super(message);
      }
   }

   public abstract static class ParticleData {
      private final Material material;
      private final byte data;
      private final int[] packetData;

      public ParticleData(Material material, byte data) {
         this.material = material;
         this.data = data;
         this.packetData = new int[]{material.getId(), data};
      }

      public Material getMaterial() {
         return this.material;
      }

      public byte getData() {
         return this.data;
      }

      public int[] getPacketData() {
         return this.packetData;
      }

      public String getPacketDataString() {
         return "_" + this.packetData[0] + "_" + this.packetData[1];
      }
   }

   private static final class ParticleDataException extends RuntimeException {
      private static final long serialVersionUID = 3203085387160737484L;

      public ParticleDataException(String message) {
         super(message);
      }
   }

   public static final class ParticlePacket {
      private static int version;
      private static Class<?> enumParticle;
      private static Constructor<?> packetConstructor;
      private static Method getHandle;
      private static Field playerConnection;
      private static Method sendPacket;
      private static boolean initialized;
      private static boolean useSendParticle;
      private final Particles effect;
      private float offsetX;
      private final float offsetY;
      private final float offsetZ;
      private final float speed;
      private final int amount;
      private final boolean longDistance;
      private final Particles.ParticleData data;
      private Object packet;

      public ParticlePacket(
         Particles effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, boolean longDistance, Particles.ParticleData data
      ) throws IllegalArgumentException {
         initialize();
         if (speed < 0.0F) {
            throw new IllegalArgumentException("The speed is lower than 0");
         } else if (amount < 0) {
            throw new IllegalArgumentException("The amount is lower than 0");
         } else {
            this.effect = effect;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.speed = speed;
            this.amount = amount;
            this.longDistance = longDistance;
            this.data = data;
         }
      }

      public ParticlePacket(Particles effect, Vector direction, float speed, boolean longDistance, Particles.ParticleData data) throws IllegalArgumentException {
         this(effect, (float)direction.getX(), (float)direction.getY(), (float)direction.getZ(), speed, 0, longDistance, data);
      }

      public ParticlePacket(Particles effect, Particles.ParticleColor color, boolean longDistance) {
         this(effect, color.getValueX(), color.getValueY(), color.getValueZ(), 1.0F, 0, longDistance, null);
         if (effect == Particles.REDSTONE && color instanceof Particles.OrdinaryColor && ((Particles.OrdinaryColor)color).getRed() == 0) {
            this.offsetX = Float.MIN_NORMAL;
         }
      }

      public static void initialize() throws Particles.ParticlePacket.VersionIncompatibleException {
         if (!initialized) {
            try {
               version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
               if (version <= 7 || version >= 13) {
                  initialized = true;
                  useSendParticle = true;
                  return;
               }

               enumParticle = PackageType.MINECRAFT_SERVER.getClass("EnumParticle");
               useSendParticle = false;
               Class<?> packetClass = PackageType.MINECRAFT_SERVER.getClass(version < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
               packetConstructor = ReflectionUtils.getConstructor(packetClass);
               getHandle = ReflectionUtils.getMethod("CraftPlayer", PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
               playerConnection = ReflectionUtils.getField("EntityPlayer", PackageType.MINECRAFT_SERVER, false, "playerConnection");
               sendPacket = ReflectionUtils.getMethod(playerConnection.getType(), "sendPacket", PackageType.MINECRAFT_SERVER.getClass("Packet"));
            } catch (Exception var1) {
               throw new Particles.ParticlePacket.VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", var1);
            }

            initialized = true;
         }
      }

      public static int getVersion() {
         if (!initialized) {
            initialize();
         }

         return version;
      }

      public static boolean isInitialized() {
         return initialized;
      }

      private void initializePacket(Location center) throws Particles.ParticlePacket.PacketInstantiationException {
         if (this.packet == null) {
            if (!useSendParticle) {
               try {
                  this.packet = packetConstructor.newInstance();
                  if (version < 8) {
                     String name = this.effect.getName();
                     if (this.data != null) {
                        name = name + this.data.getPacketDataString();
                     }

                     ReflectionUtils.setValue(this.packet, true, "a", name);
                  } else {
                     ReflectionUtils.setValue(this.packet, true, "a", enumParticle.getEnumConstants()[this.effect.getId()]);
                     ReflectionUtils.setValue(this.packet, true, "j", this.longDistance);
                     if (this.data != null) {
                        int[] packetData = this.data.getPacketData();
                        ReflectionUtils.setValue(
                           this.packet, true, "k", this.effect == Particles.ITEM_CRACK ? packetData : new int[]{packetData[0] | packetData[1] << 12}
                        );
                     }
                  }

                  ReflectionUtils.setValue(this.packet, true, "b", (float)center.getX());
                  ReflectionUtils.setValue(this.packet, true, "c", (float)center.getY());
                  ReflectionUtils.setValue(this.packet, true, "d", (float)center.getZ());
                  ReflectionUtils.setValue(this.packet, true, "e", this.offsetX);
                  ReflectionUtils.setValue(this.packet, true, "f", this.offsetY);
                  ReflectionUtils.setValue(this.packet, true, "g", this.offsetZ);
                  ReflectionUtils.setValue(this.packet, true, "h", this.speed);
                  ReflectionUtils.setValue(this.packet, true, "i", this.amount);
               } catch (Exception var5) {
                  throw new Particles.ParticlePacket.PacketInstantiationException("Packet instantiation failed", var5);
               }
            } else if (this.effect == Particles.REDSTONE) {
               int r = (int)(this.offsetX * 255.0F);
               int g = (int)(this.offsetY * 255.0F);
               int b = (int)(this.offsetZ * 255.0F);
               if (r == 0 && g == 0 && b == 0) {
                  r = 255;
               }

               center.getWorld().spawnParticle(Particle.valueOf(this.effect.toString()), center, 0, new DustOptions(Color.fromRGB(r, g, b), 1.0F));
            } else if (this.effect == Particles.SPELL_MOB || this.effect == Particles.SPELL_MOB_AMBIENT) {
               center.getWorld().spawnParticle(Particle.valueOf(this.effect.toString()), center, this.amount, this.offsetX, this.offsetY, this.offsetZ, 1.0);
            } else if (this.effect == Particles.NOTE) {
               center.getWorld().spawnParticle(Particle.valueOf(this.effect.toString()), center, this.amount, this.offsetX, 0.0, 0.0, 1.0);
            } else if (this.effect == Particles.ITEM_CRACK) {
               center.getWorld()
                  .spawnParticle(
                     Particle.valueOf(this.effect.toString()),
                     center,
                     this.amount,
                     this.offsetX,
                     this.offsetY,
                     this.offsetZ,
                     new ItemStack(this.data.getMaterial())
                  );
            } else if (this.effect == Particles.BLOCK_CRACK) {
               center.getWorld()
                  .spawnParticle(
                     Particle.valueOf(this.effect.toString()),
                     center,
                     this.amount,
                     this.offsetX,
                     this.offsetY,
                     this.offsetZ,
                     this.data.getMaterial().createBlockData()
                  );
            } else {
               center.getWorld()
                  .spawnParticle(
                     Particle.valueOf(this.effect.toString()),
                     center.getX(),
                     center.getY(),
                     center.getZ(),
                     this.amount,
                     this.offsetX,
                     this.offsetY,
                     this.offsetZ,
                     this.speed
                  );
            }
         }
      }

      public void sendTo(Location center, Player player) throws Particles.ParticlePacket.PacketInstantiationException, Particles.ParticlePacket.PacketSendingException {
         this.initializePacket(center);
         if (!useSendParticle) {
            try {
               sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), this.packet);
            } catch (Exception var4) {
               throw new Particles.ParticlePacket.PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", var4);
            }
         }
      }

      public void sendTo(Location center, List<Player> players) throws IllegalArgumentException {
         if (useSendParticle) {
            this.initializePacket(center);
         } else if (players.isEmpty()) {
            throw new IllegalArgumentException("The player list is empty");
         } else {
            for (Player player : players) {
               this.sendTo(center, player);
            }
         }
      }

      public void sendTo(Location center, double range) throws IllegalArgumentException {
         if (useSendParticle) {
            this.initializePacket(center);
         } else if (range < 1.0) {
            throw new IllegalArgumentException("The range is lower than 1");
         } else {
            String worldName = center.getWorld().getName();
            double squared = range * range;

            for (Player player : Bukkit.getOnlinePlayers()) {
               if (player.getWorld().getName().equals(worldName) && !(player.getLocation().distanceSquared(center) > squared)) {
                  this.sendTo(center, player);
               }
            }
         }
      }

      private static final class PacketInstantiationException extends RuntimeException {
         private static final long serialVersionUID = 3203085387160737484L;

         public PacketInstantiationException(String message, Throwable cause) {
            super(message, cause);
         }
      }

      private static final class PacketSendingException extends RuntimeException {
         private static final long serialVersionUID = 3203085387160737484L;

         public PacketSendingException(String message, Throwable cause) {
            super(message, cause);
         }
      }

      private static final class VersionIncompatibleException extends RuntimeException {
         private static final long serialVersionUID = 3203085387160737484L;

         public VersionIncompatibleException(String message, Throwable cause) {
            super(message, cause);
         }
      }
   }

   public static enum ParticleProperty {
      REQUIRES_WATER,
      REQUIRES_DATA,
      DIRECTIONAL,
      COLORABLE;

      private ParticleProperty() {
      }
   }

   private static final class ParticleVersionException extends RuntimeException {
      private static final long serialVersionUID = 3203085387160737484L;

      public ParticleVersionException(String message) {
         super(message);
      }
   }
}
