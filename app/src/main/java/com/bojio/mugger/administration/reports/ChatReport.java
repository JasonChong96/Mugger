package com.bojio.mugger.administration.reports;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;

public class ChatReport extends Report{
  private String message;
  private Button.OnClickListener listener;


  protected ChatReport() {

  }

  @Override
  public Button.OnClickListener getOnClickListener() {
    if (listener == null) {
      listener = v -> {
        Intent intent = new Intent(v.getContext(), ChatReportDetailsActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("report", this);
        intent.putExtras(b);
        v.getContext().startActivity(intent);
      };
    }
    return listener;
  }

  public ChatReport(Parcel in) {
    super(in);
    this.message = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(message);
  }

  public static final Creator<ChatReport> CREATOR = new Creator<ChatReport>() {
    @Override
    public ChatReport createFromParcel(Parcel in) {
      return new ChatReport(in);
    }

    @Override
    public ChatReport[] newArray(int size) {
      return new ChatReport[size];
    }
  };

  protected ChatReport(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
