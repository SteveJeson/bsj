package com.zdzc.collector.receiver.entity;

/**
 * 博实结协议实体类
 * @Author liuwei
 * @Description
 * @Date 2018/12/25 11:58
 */
public class BsjProtocol extends Protocol {

    /**
     * GPS实时补传
     */
    private int gpsFill;

    /**
     * 电压等级
     */
    private int voltageLevel;

    /**
     * GSM信号强度等级
     */
    private int signLevel;

    /**
     * 车辆状态信息
     */
    private int vehicleStatus;

    /**
     * 报警状态信息
     */
    private int alarmStatus;

    /**
     * 当日里程
     */
    private double mile;

    /**
     * 总里程
     */
    private double miles;

    /**
     * 数据类型
     */
    private int msgType;

    public int getGpsFill() {
        return gpsFill;
    }

    public void setGpsFill(int gpsFill) {
        this.gpsFill = gpsFill;
    }

    public int getVoltageLevel() {
        return voltageLevel;
    }

    public void setVoltageLevel(int voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    public int getSignLevel() {
        return signLevel;
    }

    public void setSignLevel(int signLevel) {
        this.signLevel = signLevel;
    }

    public int getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(int vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public int getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(int alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public double getMile() {
        return mile;
    }

    public void setMile(double mile) {
        this.mile = mile;
    }

    public double getMiles() {
        return miles;
    }

    public void setMiles(double miles) {
        this.miles = miles;
    }

    @Override
    public String toString() {
        return "BsjProtocol{" +
                "deviceCode=" + super.getDeviceCode() +
                ",gpsFill=" + gpsFill +
                ", voltageLevel=" + voltageLevel +
                ", signLevel=" + signLevel +
                ", vehicleStatus=" + vehicleStatus +
                ", alarmStatus=" + alarmStatus +
                ", mile=" + mile +
                ", miles=" + miles +
                '}';
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
