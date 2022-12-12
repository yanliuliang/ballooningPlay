package com.example.ballooningdemo;

import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ballooningdemo.databinding.ActivityMainBinding;
import com.example.ballooningdemo.manager.ActionManager;
import com.example.ballooningdemo.view.BallooningView;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addBall(0,3,30f,100f,140f);
        addBall(1,5,20f,200f,340f);
    }

    private void addBall(int serialNumber,int lifeValue,Float size,Float leftMargin, Float topMargin) {
        BallooningView ballooningView =new BallooningView(this);
        ballooningView.setLifeValue(lifeValue);
        ballooningView.setBallClick(() -> {
            binding.layoutParent.removeViewAt(serialNumber);
        });
        binding.layoutParent.addView(ballooningView,serialNumber);
        ActionManager actionManager= new ActionManager().getInstance(this,ballooningView);
        actionManager.setImageView(size,leftMargin,topMargin);
        ballooningView.postDelayed(() ->{
            actionManager.initAnimal(true,true);
            actionManager.initEvent(this);
        }, 1000);
    }
}