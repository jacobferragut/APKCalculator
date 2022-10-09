package com.example.calculator;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;

//Calculator guide
//https://www.youtube.com/watch?v=X3KQdwVlo1Q
//Pop-up Menu guide
//https://www.geeksforgeeks.org/popup-menu-in-android-with-example/#:~:text=Go%20to%20app%20%3E%20res%20%3E%20right-click%20%3E,menu%20resource%20file%20and%20name%20it%20as%20popup_menu.
//Alert guide
//https://www.youtube.com/watch?v=7vWoi8j5vL4

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView resultTv,solutionTv;
    MaterialButton buttonDivide,buttonMultiply,buttonPlus,buttonMinus,buttonEquals;
    MaterialButton button0,button1,button2,button3,button4,button5,button6,button7,button8,button9;
    MaterialButton buttonAC,buttonDot;
    ImageButton menuButton;

    String historyString = "";
    Boolean themeToggle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Menu Button Functionality
        menuButton = (ImageButton) findViewById(R.id.clickBtn);
        menuButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Initialize the popup menu
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuButton);

                // Setup popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // Show Calculator History
                        if (menuItem.getTitle().equals("History")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(true);
                            builder.setTitle("History");
                            builder.setMessage(historyString);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i){
                                    dialogInterface.cancel();
                                }
                            });
                            builder.show();
                        }
                        // Toggle the theme setting
                        if (menuItem.getTitle().equals("Theme")){
                            if (themeToggle){
                                LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.midPart);
                                mLinearLayout.setBackgroundColor(Color.parseColor("#1a1a70"));
                                LinearLayout bLinearLayout = (LinearLayout)findViewById(R.id.buttons_layout);
                                bLinearLayout.setBackgroundColor(Color.parseColor("#1a1a70"));
                                themeToggle = false;
                            }else{
                                LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.midPart);
                                mLinearLayout.setBackgroundColor(Color.parseColor("#44326e"));
                                LinearLayout bLinearLayout = (LinearLayout)findViewById(R.id.buttons_layout);
                                bLinearLayout.setBackgroundColor(Color.parseColor("#44326e"));
                                themeToggle = true;
                            }

                            Toast.makeText(MainActivity.this, "Change " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "View " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });

        // Calculator button onClickListener start

        resultTv = findViewById(R.id.result_tv);
        solutionTv = findViewById(R.id.solution_tv);

        assignId(buttonDivide, R.id.button_divide);
        assignId(buttonMultiply, R.id.button_multiply);
        assignId(buttonPlus, R.id.button_plus);
        assignId(buttonMinus, R.id.button_minus);
        assignId(buttonEquals, R.id.button_equals);
        assignId(button0, R.id.button_0);
        assignId(button1, R.id.button_1);
        assignId(button2, R.id.button_2);
        assignId(button3, R.id.button_3);
        assignId(button4, R.id.button_4);
        assignId(button5, R.id.button_5);
        assignId(button6, R.id.button_6);
        assignId(button7, R.id.button_7);
        assignId(button8, R.id.button_8);
        assignId(button9, R.id.button_9);
        assignId(buttonAC, R.id.button_ac);
        assignId(buttonDot, R.id.button_dot);

    }
    // find button views from xml by id and start onClickListener
    void assignId(MaterialButton btn, int id){
        btn = findViewById(id);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        MaterialButton button =(MaterialButton) view;
        String buttonText = button.getText().toString();
        // calculations are made based on button text (except ÷ and x [/ and *])
        String dataToCalculate = solutionTv.getText().toString();
        // replace ÷ and x so the calculate function inputs proper dataToCalculate string
        dataToCalculate = dataToCalculate.replace('÷','/');
        dataToCalculate = dataToCalculate.replace('x','*');

        // clear calculator screen
        if (buttonText.equals("C")){
            solutionTv.setText("");
            resultTv.setText("0");
            return;
        }
        // return calculation to solutionTv and append to historyString
        if (buttonText.equals("=")){
            historyString += solutionTv.getText().toString()+"="+resultTv.getText().toString()+"\n";
            solutionTv.setText(resultTv.getText());
            return;
        }
        // apply operator to calculation string
        else{
            dataToCalculate = dataToCalculate+buttonText;
        }

        solutionTv.setText(dataToCalculate);
        // get answer from dataToCalculate string
        String finalResult = getResult(dataToCalculate);

        if (!finalResult.equals("Err")){
            resultTv.setText(finalResult);
        }
    }

    String getResult(String data){
        try{
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            // use evaluateString function to determine finalResult
            String finalResult = context.evaluateString(scriptable,data,"Javascript",1,null).toString();
            if (finalResult.endsWith(".0")){
                finalResult = finalResult.replace(".0","");
            }
            //round to 2 decimal places
            double roundOff = (double) Math.round(Double.parseDouble(finalResult) * 100) / 100;
            finalResult = Double.toString(roundOff);

            return finalResult;
        }catch (Exception e){
            return "Err";
        }
    }
}