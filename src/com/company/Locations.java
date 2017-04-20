package com.company;

import java.io.*;
import java.util.*;

/**
 * Use RandomAccessFile so that everytime a player moves to a new locations a new location is loaded on to the
 * application without having to load all the locations on runtime
 */
public class Locations implements Map<Integer, Location> {

    private static Map<Integer, Location> locations = new LinkedHashMap<>();
    private static Map<Integer, IndexRecord> index = new LinkedHashMap<>();
    private static RandomAccessFile ra;

    public static void main(String[] args) throws IOException {

        //Set mode to read and write
        try (RandomAccessFile rao = new RandomAccessFile("locations_rand.dat", "rwd")) {

            // 1, First four bytes will contain number of locations
            // 2. The next four bytes will contain the start offset of the locations section
            // 3. The next section of the file will contain the index of the locations
            // 4. The last section contain the location records

            // A file pointer is an offset in the file where the next read/write will start form
            // If the file pointer starts a byte position 100 then the next read/write will start at byte position 100
            // The file pointer is advanced by the number of bytes we read

            //Write the number of locations at byte position 0
            //The file pointer is advanced after each write
            rao.writeInt(locations.size());

            //Calculate the length of the index. Each index record will contain 3 integers
            //To get length of each integer we have to multiply by Integer.BYTES
            int indexSize = locations.size() * 3 * Integer.BYTES;

            //Calculate the offset of the location  by calculating the current position of the file pointer
            //to the index size to account the value already written on the file
            //and also account the number of bytes of the integer to be written
            int locationStart = (int) (indexSize + rao.getFilePointer() + Integer.BYTES);

            rao.writeInt(locationStart);

            //store the location where writing the index will start
            long indexStart = rao.getFilePointer();

            // 140 1000 * index 1 .. index 2 .. index3 .. *location1 ... location 2...
            // 0 >>>>>>>> 1000byte then start writing locations here
            // Skip to the section to write the locations first because each index record requires the offset of the
            //location
            int startPointer = locationStart;
            rao.seek(startPointer);

            for (Location location : locations.values()) {
                // 1 im at work w,1,s,2
                // 1000 >> 1100
                // 2 im at home q,0,s,3
                // 1100 >> 1150
                rao.writeInt(location.getLocationID());
                rao.writeUTF(location.getDescription());

                //Create a string for the exits using StringBuilder
                //example output : W,1,S,2
                StringBuilder builder = new StringBuilder();
                for (String direction : location.getExits().keySet()) {
                    if (!direction.equalsIgnoreCase("Q")) {
                        builder.append(direction);
                        builder.append(",");
                        builder.append(location.getExits().get(direction));
                        builder.append(",");
                    }
                }
                rao.writeUTF(builder.toString());


                //Create a new index record and store it in the index LinkedHashmap
                //startPointer == offset of the record
                //rao.getFilePointer - startPointer == length of the record
                IndexRecord record = new IndexRecord(startPointer, (int) (rao.getFilePointer() - startPointer));
                index.put(location.getLocationID(), record);

                startPointer = (int) rao.getFilePointer();
            }

            //return to the start of the section of the index
            // 1 1001 1100 2 1100 1150
            rao.seek(indexStart);
            for (Integer locationID : index.keySet()) {
                rao.writeInt(locationID);
                rao.writeInt(index.get(locationID).getStartByte());
                rao.writeInt(index.get(locationID).getLength());
            }

        }


    }


    static {

        try {

            //Only populate the index hashmap. locations hashmap is not populated
            ra = new RandomAccessFile("locations_rand.dat", "rwd");

            //Remember that 1st section is the size of the of the locations map
            //2nd section is the locations' offset
            //3rd section is the indexes itself
            // 4th section is the  locations themselves
            int numLocations = ra.readInt();
            long locationStartPoint = ra.readInt();

            while (ra.getFilePointer() < locationStartPoint) {
                int locationId = ra.readInt();
                int locationStart = ra.readInt();
                int locationLength = ra.readInt();

                IndexRecord record = new IndexRecord(locationStart, locationLength);
                index.put(locationId, record);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        try(ObjectInputStream locFile = new ObjectInputStream(new BufferedInputStream(new FileInputStream("locations.dat")))){
//            boolean eof =false;
//            while(!eof){
//                try{
//                    Location location = (Location) locFile.readObject();
//                    System.out.println("Read location " + location.getLocationID() + " : " + location.getDescription());
//                    System.out.println("Found " + location.getExits().size() + " exits");
//
//                    locations.put(location.getLocationID(), location);
//                }catch (EOFException e){
//                    eof = true;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch(ClassNotFoundException e){
//            System.out.println("ClassNotFoundException" + e.getMessage());
//        }

//        try(DataInputStream locFile = new DataInputStream((new BufferedInputStream(new FileInputStream("locations.dat"))))){
//            boolean eof = false;
//            while(!eof){
//                try{
//                    Map<String, Integer> exits = new LinkedHashMap<>();
//                    int locID = locFile.readInt();
//                    String description = locFile.readUTF();
//                    int numbExits = locFile.readInt();
//                    System.out.println("Read location " + locID + " : "+ description);
//                    System.out.println("Found " + numbExits + " exits");
//                    for(int i=0; i <numbExits; i++){
//                        String direction = locFile.readUTF();
//                        int destination = locFile.readInt();
//                        exits.put(direction, destination);
//                        System.out.println( "\t\t" + direction + "," + destination);
//                    }
//                    locations.put(locID, new Location(locID, description, exits));
//                }catch(EOFException e){
//                    eof = true;
//                }
//            }
//
//        } catch(IOException io) {
//            System.out.println("IO Exception");
//        }
    }

    public Location getLocation(int locationId) throws IOException {
        IndexRecord record = index.get(locationId);
        ra.seek(record.getStartByte());

        // 1 im at work Q,1,S,2 2 im at hoME S,1
        int id = ra.readInt();
        String description = ra.readUTF();
        String exits = ra.readUTF();
        String[] exitPart = exits.split(",");

        Location location = new Location(locationId, description, null);

        if (locationId != 0) {
            //Q,1,S,2
            for (int i = 0; i < exitPart.length; i++) {
                System.out.println("exitPart = " + exitPart[i]);
                System.out.println("exitPart[+1] = " + exitPart[i + 1]);
                String direction = exitPart[i];
                int destination = Integer.parseInt(exitPart[++i]);
                location.addExit(direction, destination);
            }
        }

        return location;

    }

    public void initialize() throws Exception {


    }

    @Override
    public int size() {
        return locations.size();
    }

    @Override
    public boolean isEmpty() {
        return locations.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return locations.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return locations.containsValue(value);
    }

    @Override
    public Location get(Object key) {
        return locations.get(key);
    }

    @Override
    public Location put(Integer key, Location value) {
        return locations.put(key, value);
    }

    @Override
    public Location remove(Object key) {
        return locations.remove(key);
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends Location> m) {

    }

    @Override
    public void clear() {
        locations.clear();
    }

    @Override
    public Set<Integer> keySet() {
        return locations.keySet();
    }

    @Override
    public Collection<Location> values() {
        return locations.values();
    }

    @Override
    public Set<Entry<Integer, Location>> entrySet() {
        return locations.entrySet();
    }
}
