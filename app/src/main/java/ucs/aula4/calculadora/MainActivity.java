package ucs.aula4.calculadora;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    LinkedList<Double> numberList = new LinkedList<>();
    LinkedList<Character> operationList = new LinkedList<>();
    AtomicReference<String> lastNumber  = new AtomicReference<>("");
    EditText txtNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btnResult = findViewById(R.id.btnResult);
        final Button btnDot = findViewById(R.id.btnDot);
        final Button btnDel = findViewById(R.id.btnDelete);
        final Button btnAbre = findViewById(R.id.btnAbre);
        final Button btnFecha = findViewById(R.id.btnFecha);
        final Button btnClr = findViewById(R.id.btnClr);
        txtNumber = findViewById(R.id.txtNumber);
        txtNumber.setRawInputType(InputType.TYPE_NULL);
        txtNumber.setTextIsSelectable(true);

        btnDel.setOnClickListener(v -> {
            String txt = txtNumber.getText().toString();
            txt = txt.length() > 0 ? txt.substring(0,txt.length() - 1) : txt;
            txtNumber.setText(txt);
            txt = lastNumber.get();
            txt = txt.length() > 0 ? txt.substring(0,txt.length() - 1) : txt;
            lastNumber.set(txt);
            scrollDown(txtNumber);
        });
        btnAbre.setOnClickListener(v -> {
            Button btn = (Button)v;
            String txt = txtNumber.getText().toString() + btn.getText().toString();
            txtNumber.setText(txt);
            scrollDown(txtNumber);
        });
        btnFecha.setOnClickListener(v -> {
            Button btn = (Button)v;
            String txt = txtNumber.getText().toString() + btn.getText().toString();
            txtNumber.setText(txt);
            scrollDown(txtNumber);
        });
        btnDot.setOnClickListener(v -> {
            if(lastNumber.get().contains(".") || txtNumber.getText().toString().isEmpty()|| lastIsOperation() ) return;
            Button btn = (Button)v;
            String txt = txtNumber.getText().toString() + btn.getText().toString();
            String n = lastNumber.get()  + btn.getText().toString();
            lastNumber.set(n);
            txtNumber.setText(txt);
            scrollDown(txtNumber);
        });
        btnClr.setOnClickListener(v -> {
            String txt = "";
            lastNumber.set("");
            txtNumber.setText(txt);
            scrollDown(txtNumber);
        });
        btnResult.setOnClickListener(v -> {
            if(txtNumber.getText().toString().isEmpty() || lastIsOperation()) return;
            lastNumber.set("");
            calc(txtNumber.getText().toString());
        });
    }

    private void invertPlus(Button btn) {
        char lastChar = txtNumber.getText().charAt(txtNumber.getText().length() - 1);
        if (lastChar == '-' || lastChar == '+') {
            String newStr = txtNumber.getText().toString().substring(0, txtNumber.getText().length() - 1) + btn.getText().toString();
            txtNumber.setText(newStr);
        }
    }

    public void operatorClick(View view){
        Button btn = (Button)view;
        if(txtNumber.getText().toString().isEmpty()){
            if (btn.getText().toString().equals("-")) {
                numbersClick(view);
            }
            return;
        }
        if(lastIsOperation()) {
            if(txtNumber.getText().toString().length() != 1)
            switch(btn.getText().toString().charAt(0)){
                case '+':
                case '-':
                    invertPlus(btn);
                    break;
            }
        } else {
            lastNumber.set("");
            String txt = txtNumber.getText().toString() + btn.getText().toString();
            txtNumber.setText(txt);
            scrollDown(txtNumber);
        }
    }

    private boolean lastIsOperation(){
        if(txtNumber.getText().toString().isEmpty()) return false;
        switch (txtNumber.getText().charAt(txtNumber.getText().length() - 1)){
            case '+':
            case '-':
            case '/':
            case '*':
                return true;
            default:
                return false;
        }
    }
    private void scrollDown(@NonNull EditText messageView) {
        messageView.setSelection(messageView.getText().length());
    }
    protected Object calcula(String s){
        s = s.replace("(","").replace(")","");
        Pattern pattern = Pattern.compile("([0-9]+\\.*[0-9]*)+[\\/*]+([0-9]+\\.*[0-9]*)");
        Matcher m = pattern.matcher(s);
        Log.i("string s", "oq chegou: " + s);
        double result;
        while(m.find()){
            String[] numbers = m.group().split("[\\/*]");

            //Log.i("split", "numbers : " + s);
            if(s.contains("*")){
                result = Double.parseDouble(numbers[0]) * Double.parseDouble(numbers[1]);
            } else {
                if(Double.parseDouble(numbers[1]) == 0) throw new NumberFormatException("Não pode dividir por zero!");
                result = Double.parseDouble(numbers[0]) / Double.parseDouble(numbers[1]);
            }
            s = s.replace(m.group(), String.valueOf(result));
            m = pattern.matcher(s);
        }
        pattern = Pattern.compile("([+-]*([0-9]+\\.*[0-9]*))");
        m = pattern.matcher(s);
        if(m.find()) {
            double total = 0;
            do {
                total += Double.parseDouble(m.group());
            } while (m.find());
            return total;
        }
        else return s;
    }

    private void calc(String s){
        Pattern pattern = Pattern.compile("(\\(([^()]+)\\))");
        Matcher m = pattern.matcher(s);
        while(m.find()) {
            s = s.replace(m.group(), calcula(m.group()).toString());
            m = pattern.matcher(s);
        }
        Object result;
        try {
            result = calcula(s);
            String text = (double)result % 1 == 0 ? String.format("%.0f",result) : String.valueOf(result);
            txtNumber.setText(text);
        } catch (Exception e){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder.setTitle("Erro!");

                alertDialogBuilder
                        .setMessage(e.getMessage())
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {});
                alertDialogBuilder.show();
                txtNumber.setText("");
        }
    }

    private void setError(){
        operationList.clear();
        numberList.clear();
        lastNumber.set("");
        txtNumber.setText("ERR");
    }

    private boolean isError(){
        return txtNumber.getText().toString().toUpperCase(Locale.ROOT).equals("ERR");
    }

    public void numbersClick(View view) {
        Button btn = (Button)view;
        String txt = !isError() ? (txtNumber.getText().toString() + btn.getText().toString()) : btn.getText().toString();
        String n = !isError() ? lastNumber.get()  + btn.getText().toString() : btn.getText().toString();
        lastNumber.set(n);
        txtNumber.setText(txt);
        scrollDown(txtNumber);
    }
}