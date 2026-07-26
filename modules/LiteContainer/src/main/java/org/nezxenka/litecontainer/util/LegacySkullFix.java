package org.nezxenka.litecontainer.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Restores skull textures on player-head ItemStacks that were serialized by
 * CraftBukkit 1.16.5 into a YAML config (data.yml).
 *
 * <p>On 1.16.5 {@code CraftMetaSkull} stashed the head's GameProfile into the
 * opaque {@code internal} field of the serialized map, using the legacy
 * GameProfile NBT layout:
 *
 * <pre>
 * SkullProfile: { Properties: { textures: [ { Value: "&lt;base64&gt;" } ] }, Id: [I; ...] }
 * </pre>
 *
 * <p>Since 1.20.5 the head profile is a {@code minecraft:profile} data component and
 * 1.21.4's {@code CraftMetaSkull.deserializeInternal} feeds that same compound to
 * {@code ResolvableProfile.CODEC}, which expects the component layout instead
 * ({@code name} / {@code id} / {@code properties} as a lower-case list). Every field
 * in that codec is optional, so parsing the legacy compound *succeeds* and silently
 * yields an empty profile - the textures are dropped and the head renders as the
 * default Steve/Alex skin.
 *
 * <p>This helper reads the legacy blob itself and re-applies the texture through the
 * modern Paper profile API.
 */
public final class LegacySkullFix {

   private static final int TAG_END = 0;
   private static final int TAG_BYTE = 1;
   private static final int TAG_SHORT = 2;
   private static final int TAG_INT = 3;
   private static final int TAG_LONG = 4;
   private static final int TAG_FLOAT = 5;
   private static final int TAG_DOUBLE = 6;
   private static final int TAG_BYTE_ARRAY = 7;
   private static final int TAG_STRING = 8;
   private static final int TAG_LIST = 9;
   private static final int TAG_COMPOUND = 10;
   private static final int TAG_INT_ARRAY = 11;
   private static final int TAG_LONG_ARRAY = 12;

   private LegacySkullFix() {
   }

   /**
    * Re-applies the legacy skull texture to {@code item} if it lost it during
    * deserialization.
    *
    * @param item             the freshly deserialized item, may be {@code null}
    * @param internalBase64   the raw {@code internal} value from data.yml, may be {@code null}
    * @return {@code true} if the item was repaired
    */
   public static boolean repair(ItemStack item, String internalBase64) {
      if (item == null || internalBase64 == null || internalBase64.isEmpty()) {
         return false;
      }
      if (item.getType() != Material.PLAYER_HEAD) {
         return false;
      }

      ItemMeta meta = item.getItemMeta();
      if (!(meta instanceof SkullMeta)) {
         return false;
      }

      SkullMeta skullMeta = (SkullMeta) meta;
      PlayerProfile current = skullMeta.getPlayerProfile();
      if (current != null && current.hasTextures()) {
         return false;
      }

      LegacyProfile legacy = parse(internalBase64);
      if (legacy == null || legacy.texture == null) {
         return false;
      }

      PlayerProfile profile = Bukkit.createProfile(legacy.id != null ? legacy.id : UUID.randomUUID(), null);
      profile.setProperty(new ProfileProperty("textures", legacy.texture));
      skullMeta.setPlayerProfile(profile);
      item.setItemMeta(skullMeta);
      return true;
   }

   /** The bits of a legacy {@code SkullProfile} compound we care about. */
   public static final class LegacyProfile {
      public final String texture;
      public final UUID id;

      LegacyProfile(String texture, UUID id) {
         this.texture = texture;
         this.id = id;
      }
   }

   /** Parses the gzipped NBT {@code internal} blob and pulls out the skull texture. */
   public static LegacyProfile parse(String internalBase64) {
      Map<String, Object> root;
      try {
         byte[] raw = Base64.getDecoder().decode(internalBase64);
         root = readCompressed(raw);
      } catch (Exception e) {
         return null;
      }
      if (root == null) {
         return null;
      }

      Object skullProfile = root.get("SkullProfile");
      if (!(skullProfile instanceof Map)) {
         return null;
      }
      Map<?, ?> profile = (Map<?, ?>) skullProfile;

      String texture = null;
      Object properties = profile.get("Properties");
      if (properties instanceof Map) {
         Object textures = ((Map<?, ?>) properties).get("textures");
         if (textures instanceof List) {
            for (Object entry : (List<?>) textures) {
               if (entry instanceof Map) {
                  Object value = ((Map<?, ?>) entry).get("Value");
                  if (value instanceof String && !((String) value).isEmpty()) {
                     texture = (String) value;
                     break;
                  }
               }
            }
         }
      }
      if (texture == null) {
         return null;
      }

      return new LegacyProfile(texture, readUuid(profile.get("Id")));
   }

   private static UUID readUuid(Object id) {
      if (id instanceof int[]) {
         int[] a = (int[]) id;
         if (a.length == 4) {
            return new UUID(
               (long) a[0] << 32 | (long) a[1] & 4294967295L,
               (long) a[2] << 32 | (long) a[3] & 4294967295L
            );
         }
      } else if (id instanceof String) {
         try {
            return UUID.fromString((String) id);
         } catch (IllegalArgumentException ignored) {
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   private static Map<String, Object> readCompressed(byte[] raw) throws IOException {
      try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(raw)))) {
         int type = in.readUnsignedByte();
         if (type != TAG_COMPOUND) {
            return null;
         }
         in.readUTF();
         return (Map<String, Object>) readPayload(in, TAG_COMPOUND);
      }
   }

   private static Object readPayload(DataInputStream in, int type) throws IOException {
      switch (type) {
         case TAG_END:
            return null;
         case TAG_BYTE:
            return in.readByte();
         case TAG_SHORT:
            return in.readShort();
         case TAG_INT:
            return in.readInt();
         case TAG_LONG:
            return in.readLong();
         case TAG_FLOAT:
            return in.readFloat();
         case TAG_DOUBLE:
            return in.readDouble();
         case TAG_BYTE_ARRAY: {
            byte[] bytes = new byte[in.readInt()];
            in.readFully(bytes);
            return bytes;
         }
         case TAG_STRING:
            return in.readUTF();
         case TAG_LIST: {
            int elementType = in.readUnsignedByte();
            int length = in.readInt();
            List<Object> list = new ArrayList<>(Math.max(0, length));
            for (int i = 0; i < length; i++) {
               list.add(readPayload(in, elementType));
            }
            return list;
         }
         case TAG_COMPOUND: {
            Map<String, Object> map = new HashMap<>();
            while (true) {
               int entryType = in.readUnsignedByte();
               if (entryType == TAG_END) {
                  return map;
               }
               map.put(in.readUTF(), readPayload(in, entryType));
            }
         }
         case TAG_INT_ARRAY: {
            int[] ints = new int[in.readInt()];
            for (int i = 0; i < ints.length; i++) {
               ints[i] = in.readInt();
            }
            return ints;
         }
         case TAG_LONG_ARRAY: {
            long[] longs = new long[in.readInt()];
            for (int i = 0; i < longs.length; i++) {
               longs[i] = in.readLong();
            }
            return longs;
         }
         default:
            throw new IOException("Unknown NBT tag type " + type);
      }
   }
}
