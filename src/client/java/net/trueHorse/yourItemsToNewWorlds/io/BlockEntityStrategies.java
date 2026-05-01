package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BlockEntityStrategies {

    private static final Map<String, Function<NbtCompound,NbtList>> STRATEGIES = Map.ofEntries(
            Map.entry("BlockDrive",(driveNbt)->{
                NbtList itemNbts = new NbtList();
                NbtCompound inv = driveNbt.getCompound("inv");

                for(int i = 0;i<10;i++){
                    NbtCompound tag = inv.getCompound("item"+i).getCompound("tag");
                    List<String> itemKeys = tag.getKeys().stream().filter(key->key.charAt(0)== '#').toList();

                    for(String key : itemKeys){
                        NbtCompound item = tag.getCompound(key);
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
                NbtList itemNbts = new NbtList();
                NbtList storedNbts = new NbtList();
                List<String> inventoryKeys = driveNbt.getKeys().stream().filter(key->key.startsWith("Inventory_")).toList();

                for(String key:inventoryKeys){
                    driveNbt.getList(key,10).forEach(inv->storedNbts.addAll(((NbtCompound)inv).getCompound("tag").getList("Items",10)));
                }

                for(NbtElement el : storedNbts){
                    NbtCompound itemNbt = (NbtCompound) el;
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

    private static final Function<NbtCompound,NbtList> DEFAULT_STRATEGY = (blockEntityNbt-> blockEntityNbt.getList("Items", 10)
    );

    public static Function<NbtCompound,NbtList> getStrategy(NbtCompound blockEntityNbt){
        String id = blockEntityNbt.getString("id");
        return STRATEGIES.getOrDefault(id, DEFAULT_STRATEGY);
    }
}
