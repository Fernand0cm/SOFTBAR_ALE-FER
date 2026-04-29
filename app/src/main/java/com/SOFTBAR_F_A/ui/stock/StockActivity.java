package com.SOFTBAR_F_A.ui.stock;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.common.Header;

public class StockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        Header.aplica(this, getString(R.string.stock_title));
    }
}
