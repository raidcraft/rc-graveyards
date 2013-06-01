package de.raidcraft.rcgraveyards.util;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author Philip Urban
 */
public class BiomeFaker {

    private static final int BYTES_PER_NIBBLE_PART = 2048;
    private static final int CHUNK_SEGMENTS = 16;
    private static final int NIBBLES_REQUIRED = 4;
    private static final int BIOME_ARRAY_LENGTH = 256;

    // Look this up in net.minecraft.server.Biome
    private static final byte BIOME_HELL = 8;

    // Used to pass around detailed information about chunks
    private static class ChunkInfo {
        public int chunkX;
        public int chunkZ;
        public int chunkMask;
        public int extraMask;
        public int chunkSectionNumber;
        public int extraSectionNumber;
        public boolean hasContinous;
        public byte[] data;
        public Player player;
        public int startIndex;
        public int size;
    }

    public static void translateMapChunk(PacketContainer packet, Player player) throws FieldAccessException {
        StructureModifier<Integer> ints = packet.getIntegers();
        StructureModifier<byte[]> byteArray = packet.getByteArrays();

        // Create an info objects
        ChunkInfo info = new ChunkInfo();
        info.player = player;
        info.chunkX = ints.read(0);     // packet.a;
        info.chunkZ = ints.read(1);     // packet.b;
        info.chunkMask = ints.read(2);  // packet.c;
        info.extraMask = ints.read(3);  // packet.d;
        info.hasContinous = getOrDefault(packet.getBooleans().readSafely(0), true);
        info.data = byteArray.read(1);  // packet.inflatedBuffer;
        info.startIndex = 0;

        translateChunkInfo(info, info.data);
    }

    // Mimic the ?? operator in C#
    private static <T> T getOrDefault(T value, T defaultIfNull) {
        return value != null ? value : defaultIfNull;
    }

    public static void translateMapChunkBulk(PacketContainer packet, Player player) throws FieldAccessException {
        StructureModifier<int[]> intArrays = packet.getIntegerArrays();
        StructureModifier<byte[]> byteArrays = packet.getSpecificModifier(byte[].class);

        int[] x = intArrays.read(0); // getPrivateField(packet, "c");
        int[] z = intArrays.read(1); // getPrivateField(packet, "d");

        ChunkInfo[] infos = new ChunkInfo[x.length];

        int dataStartIndex = 0;
        int[] chunkMask = intArrays.read(2); // packet.a;
        int[] extraMask = intArrays.read(3); // packet.b;

        for (int chunkNum = 0; chunkNum < infos.length; chunkNum++) {
            // Create an info objects
            ChunkInfo info = new ChunkInfo();
            infos[chunkNum] = info;
            info.player = player;
            info.chunkX = x[chunkNum];
            info.chunkZ = z[chunkNum];
            info.chunkMask = chunkMask[chunkNum];
            info.extraMask = extraMask[chunkNum];
            info.hasContinous = true; // Always TRUE here
            info.data = byteArrays.read(1); //packet.buildBuffer;
            info.startIndex = dataStartIndex;

            translateChunkInfo(info, info.data);
            dataStartIndex += info.size;
        }
    }

    private static void translateChunkInfo(ChunkInfo info, byte[] returnData) {
        // Compute chunk number
        for (int i = 0; i < CHUNK_SEGMENTS; i++) {
            if ((info.chunkMask & (1 << i)) > 0) {
                info.chunkSectionNumber++;
            }
            if ((info.extraMask & (1 << i)) > 0) {
                info.extraSectionNumber++;
            }
        }

        // There's no sun/moon in the end or in the nether, so Minecraft doesn't sent any skylight information
        // This optimization was added in 1.4.6. Note that ideally you should get this from the "f" (skylight) field.
        int skylightCount = info.player.getWorld().getEnvironment() == World.Environment.NORMAL ? 1 : 0;

        // To calculate the size of each chunk, we need to take into account the number of segments (out of 16)
        // that have been sent. Each segment sent is encoded in the chunkMask bit field, where every binary 1
        // indicates that a segment is present and every 0 indicates that it's not.

        // The total size of a chunk is the number of blocks sent (depends on the number of sections) multiplied by the
        // amount of bytes per block. This last figure can be calculated by adding together all the data parts:
        //   For any block:
        //    * Block ID          -   8 bits per block (byte)
        //    * Block metadata    -   4 bits per block (nibble)
        //    * Block light array -   4 bits per block
        //   If 'worldProvider.skylight' is TRUE
        //    * Sky light array   -   4 bits per block
        //   If the segment has extra data:
        //    * Add array         -   4 bits per block
        //   Biome array - only if the entire chunk (has continous) is sent:
        //    * Biome array       -   256 bytes
        //
        // A section has 16 * 16 * 16 = 4096 blocks.
        info.size = BYTES_PER_NIBBLE_PART * (
                (NIBBLES_REQUIRED + skylightCount) * info.chunkSectionNumber +
                        info.extraSectionNumber) +
                (info.hasContinous ? BIOME_ARRAY_LENGTH : 0);

        if (info.hasContinous) {
            int biomeStart = info.startIndex + info.size - BIOME_ARRAY_LENGTH;
            byte value = (byte) (info.chunkX + info.chunkZ % 22);

            for (int i = 0; i < BIOME_ARRAY_LENGTH; i++) {
                info.data[biomeStart + i] = value;
            }
        }
    }
}
