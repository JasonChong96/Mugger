package com.bojio.mugger.listings.chat;

public class Message {
  private String fromUid;
  private String fromName;
  private String content;
  private Long time;
  private Long day;

  Message(String fromUid, String fromName, String content, Long time, Long day) {
    this.fromUid = fromUid;
    this.fromName = fromName;
    this.content = content;
    this.time = time;
    this.day = day;
  }

  public Long getTime() {
    return time;
  }

  public void setTime(Long time) {
    this.time = time;
  }

  public Long getDay() {
    return day;
  }

  public void setDay(Long day) {
    this.day = day;
  }

  public String getFromUid() {
    return fromUid;
  }

  public void setFromUid(String fromUid) {
    this.fromUid = fromUid;
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
