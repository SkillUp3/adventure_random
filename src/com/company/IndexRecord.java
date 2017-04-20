package com.company;


public class IndexRecord {
    //key locationid 1
    //index 1 1001 100
    //location 1 located at byte 1001 and has a length of 100
    private int startByte;
    private int length;

    /**
     * The index stores the offset and record length of each location
     * @param startByte corresponds to the offset of the record
     * @param length corresponds to the length of the record i.e. how many bytes long the record is
     */
    public IndexRecord(int startByte, int length) {
        this.startByte = startByte;
        this.length = length;
    }

    public int getStartByte() {
        return startByte;
    }

    public void setStartByte(int startByte) {
        this.startByte = startByte;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
