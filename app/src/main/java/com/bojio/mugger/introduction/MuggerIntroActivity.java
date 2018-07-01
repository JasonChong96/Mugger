package com.bojio.mugger.introduction;

import android.os.Bundle;

import com.bojio.mugger.R;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import de.mateware.snacky.Snacky;

public class MuggerIntroActivity extends IntroActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    addSlide(new SimpleSlide.Builder()
        .title(R.string.intro_title_1)
        .description(R.string.intro_desc_1)
        .image(R.drawable.mugger_intro_1)
        .background(R.color.colorBackgroundAdmin)
        .backgroundDark(R.color.colorPrimaryAdmin)
        .scrollable(false)
        .build());
    addSlide(new SimpleSlide.Builder()
        .title(R.string.intro_title_2)
        .description(R.string.intro_desc_2)
        .image(R.drawable.mugger_intro_2)
        .background(R.color.colorBackgroundAdmin)
        .backgroundDark(R.color.colorPrimaryAdmin)
        .scrollable(false)
        .build());
    addSlide(new SimpleSlide.Builder()
        .title(R.string.intro_title_3)
        .description(R.string.intro_desc_3)
        .image(R.drawable.mugger_intro_3)
        .background(R.color.colorBackgroundAdmin)
        .backgroundDark(R.color.colorPrimaryAdmin)
        .scrollable(false)
        .build());
    addSlide(new SimpleSlide.Builder()
        .title(R.string.intro_title_4)
        .description(R.string.intro_desc_4)
        .image(R.drawable.mugger_intro_4)
        .background(R.color.colorBackgroundAdmin)
        .backgroundDark(R.color.colorPrimaryAdmin)
        .scrollable(false)
        .build());
    setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);
  }

  @Override
  public void onBackPressed() {
    if (getCurrentSlidePosition() == 0) {
      Snacky.builder()
          .setActivity(this)
          .setText("This are no slides before this.")
          .info()
          .show();
    } else {
      super.onBackPressed();
    }
  }
}