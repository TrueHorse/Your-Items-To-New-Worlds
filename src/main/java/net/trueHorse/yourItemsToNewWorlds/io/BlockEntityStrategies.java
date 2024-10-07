package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BlockEntityStrategies {

    private static final Map<String, Function<CompoundTag,ListTag>> STRATEGIES = Map.ofEntries(
            Map.entry("BlockDrive",(driveNbt)->{
                ListTag itemNbts = new ListTag();
                CompoundTag inv = driveNbt.getCompound("inv");

                for(int i = 0;i<10;i++){
                    CompoundTag tag = inv.getCompound("item"+i).getCompound("tag");
                    List<String> itemKeys = tag.getAllKeys().stream().filter(key->key.charAt(0)== '#').toList();

                    for(String key : itemKeys){
                        CompoundTag item = tag.getCompound(key);
                        long cnt = item.getLong("Cnt");

                        if(cnt>=64){
                            item.putByte("Count", (byte) 64);
                            for(int j = 0;j<Math.floor(cnt/64.0);j++){
                                itemNbts.add(item.copy());
                            }
                        }

                        item.putByte("Count",(byte)(cnt%64));
                        itemNbts.add(item);
                    }
                }

                return itemNbts;
            })/*,
            Map.entry("refinedstorage:disk_drive",(driveNbt)->{
                ListTag itemNbts = new ListTag();
                ListTag storedNbts = new ListTag();
                List<String> inventoryKeys = driveNbt.getKeys().stream().filter(key->key.startsWith("Inventory_")).toList();

                for(String key:inventoryKeys){
                    driveNbt.getList(key,10).forEach(inv->storedNbts.addAll(((CompoundTag)inv).getCompound("tag").getList("Items",10)));
                }

                for(NbtElement el : storedNbts){
                    CompoundTag itemNbt = (CompoundTag) el;
                    itemNbt.put("id",itemNbt.get("Type"));

                    int quantity = itemNbt.getInt("Quantity");
                    itemNbt.putByte("count",(byte)64);
                    for(int i = 0;i<Math.floor(quantity/64.0);i++){
                        itemNbts.add(itemNbt.copy());
                    }

                    itemNbt.putByte("count",(byte)(quantity%64));
                    itemNbts.add(itemNbt);
                }

                return itemNbts;
            })*/
    );

    private static final Function<CompoundTag,ListTag> DEFAULT_STRATEGY = (blockEntityNbt->blockEntityNbt.getList("Items", 10));

    public static Function<CompoundTag,ListTag> getStrategy(CompoundTag blockEntityNbt){
        String id = blockEntityNbt.getString("id");
        return STRATEGIES.getOrDefault(id, DEFAULT_STRATEGY);
    }
}
