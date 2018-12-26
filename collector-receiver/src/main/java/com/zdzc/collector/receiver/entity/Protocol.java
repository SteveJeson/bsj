package com.zdzc.collector.receiver.entity;

import java.util.Date;

/**
 * 通用协议父类
 * @Author liuwei
 * @Description
 * @Date 2018/12/25 11:58
 */
public class Protocol {

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 设备号
     */
    private String deviceCode;

    /**
     * 纬度
     */
    private double lat;

    /**
     * 经度
     */
    private double lon;

    /**
     * 日期时间
     */
    private Date dateTime;

    /**
     * 方向角
     */
    private double direction;

    /**
     * 定位卫星数
     */
    private int satelliteNum;

    /**
     * 速度
     */
    private double speed;

    /**
     * ICCID
     */
    private String iccid;

    /**
     * 电压
     */
    private int voltage;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public int getSatelliteNum() {
        return satelliteNum;
    }

    public void setSatelliteNum(int satelliteNum) {
        this.satelliteNum = satelliteNum;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    @Override
    public String toString() {
        return "Protocol{" +
                "protocolType='" + protocolType + '\'' +
                ", deviceCode='" + deviceCode + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", dateTime=" + dateTime +
                ", direction=" + direction +
                ", satelliteNum=" + satelliteNum +
                ", speed=" + speed +
                ", iccid='" + iccid + '\'' +
                ", voltage=" + voltage +
                '}';
    }
}
