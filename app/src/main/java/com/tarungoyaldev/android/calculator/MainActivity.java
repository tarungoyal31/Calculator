package com.tarungoyaldev.android.calculator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.tarungoyaldev.android.calculator.tools.CalculatorUtilities;
import com.tarungoyaldev.android.calculator.tools.StringObservable;
import com.tarungoyaldev.android.calculator.tools.SwipeDetector;

import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private Stack<String> calculationStack = new Stack<>();
    private StringObservable displayStringObserver = new StringObservable("");
    // Temporary stack and temporary string for moving back to previous state. These are used when
    // an operation is changed.
    private Stack<String> temporaryStack = new Stack<>();
    private String temporaryString = "";
    private OperationType lastOperation;
    private boolean isRadian = true;
    private boolean isMoreOperation = false;

    public enum OperationType {
        UNARY,
        BINARY,
        EQUAL,
        NUMBERUPDATE,
    }
    public enum Operation {
        EQUAL(0),
        NULL(0),
        ADDITION(1),
        SUBTRACTION(1),
        MULTIPLICATION(2),
        DIVISION(2),
        POW(3),
        EE(3),
        OPENBRACKET(0);


        private int value;

        Operation(int value) {
            this.value = value;
        }

        boolean isLowerPrecedence(Operation otherValue) {
            return this.value < otherValue.value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastOperation = OperationType.NUMBERUPDATE;
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.textView);
        assert textView != null;
        textView.setOnTouchListener(new SwipeDetector(new SwipeDetector.Callback() {
            @Override
            public boolean onLeftSwipe() {
                clearSingleDigit();
                return true;
            }
            @Override
            public boolean onRightSwipe() {
                clearSingleDigit();
                return true;
            }
        }));
        calculationStack.push(Operation.NULL.name());
        // Add observer to displayString. This observer updates the text in textView and change the
        // state of ClearButton according to the current text
        displayStringObserver.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                String text = (String) data;
                textView.setText(text);
                // Clear button clears the current number to 0 if there is text that is being
                // written. Otherwise it resets the whole calculation stack.
                Button clearButton = (Button) findViewById(R.id.clearButton);
                assert clearButton != null;
                if (text.equals("0")) {
                    clearButton.setText("AC");
                } else {
                    clearButton.setText("C");
                }
            }
        });
        // Support for setting layout_columnWeight and layout_rowWeight was added in android L and
        // above. This code handles the buttons layout in Kitkat and below.
        final GridLayout gridLayout = (GridLayout) findViewById(R.id.calculatorLayout);
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        for (int i = 0; i < gridLayout.getChildCount(); i++) {
                            View view = gridLayout.getChildAt(i);
                            GridLayout.LayoutParams params = (GridLayout.LayoutParams) view.getLayoutParams();
                            if (view.getId() == R.id.textView) {
                                params.width = (gridLayout.getWidth()) - params.rightMargin - params.leftMargin;
                                params.height = (gridLayout.getHeight() * 2 / (gridLayout.getRowCount() + 1)) - params.topMargin - params.bottomMargin;
                            } else {
                                params.width = (gridLayout.getWidth() / gridLayout.getColumnCount())
                                        - params.rightMargin - params.leftMargin;
                                params.height =
                                        (gridLayout.getHeight() / (gridLayout.getRowCount() + 1))
                                                - params.topMargin - params.bottomMargin;
                            }
                            view.setLayoutParams(params);
                        }
                    }
                });
    }

    /**
     * Handles click event on number buttons. This updates the number string displayed in calculator
     * according to the number button pressed.
     * @param view number button clicked
     */
    public void onNumberClick(View view) {
        if (view instanceof Button) {
            Button numberButton = (Button) view;
            String numberString = (String) numberButton.getTag();
            if (displayStringObserver.getObservedString().equals("0") ||
                    !lastOperation.equals(OperationType.NUMBERUPDATE)) {
                displayStringObserver.updateString(numberString);
            } else if (numberString.equals(".")
                    && displayStringObserver.getObservedString().contains(".")) {
                return;
            } else {
                displayStringObserver.concat(numberString);
            }
        }
        lastOperation = OperationType.NUMBERUPDATE;
    }

    /**
     * Handles different operations on the numbers. If the operation is changed by user, this
     * function makes use of temporaryStack and temporaryString to move back the state by one
     * operation
     * @param view operation button clicked.
     */
    public void onOperationClick(View view) {
        int viewId = view.getId();
        if (!lastOperation.equals(OperationType.BINARY)) {
            temporaryStack.removeAllElements();
            temporaryStack.addAll(calculationStack);
            temporaryString = displayStringObserver.getObservedString();
        }
        Operation buttonOperation;
        if (view instanceof Button) {
            switch (viewId) {
                case R.id.additionButton:
                    buttonOperation = Operation.ADDITION;
                    break;
                case R.id.subtractionButton:
                    buttonOperation = Operation.SUBTRACTION;
                    break;
                case R.id.multiplicationButton:
                    buttonOperation = Operation.MULTIPLICATION;
                    break;
                case R.id.divisionButton:
                    buttonOperation = Operation.DIVISION;
                    break;
                case R.id.powButton:
                    buttonOperation = Operation.POW;
                    break;
                case R.id.eeButton:
                    buttonOperation = Operation.EE;
                    break;
                case R.id.equalButton:
                    buttonOperation = Operation.EQUAL;
                    break;
                default:
                    buttonOperation = Operation.NULL;
                    break;
            }
            if (lastOperation.equals(OperationType.BINARY)) {
                calculationStack.removeAllElements();
                calculationStack.addAll(temporaryStack);
                displayStringObserver.updateString(temporaryString);
            }
        } else {
            return;
        }
        if (viewId != R.id.equalButton) {
            applyOperation(buttonOperation);
            lastOperation = OperationType.BINARY;
        } else {
            applyEqualOperation();
            lastOperation = OperationType.EQUAL;
        }
    }

    public void onUnaryOperationClick(View view) {
        if (view instanceof Button) {
            int viewId = view.getId();
            Button button = (Button) findViewById(viewId);
            String displayString = displayStringObserver.getObservedString();
            double textValue = CalculatorUtilities.convertStringToDouble(displayString);
            if ((textValue == Double.NEGATIVE_INFINITY || textValue == Double.POSITIVE_INFINITY)
                    && viewId != R.id.clearButton) {
                return;
            }
            switch (viewId) {
                case R.id.clearButton:
                    if (button.getText().equals("AC")) {
                        calculationStack.removeAllElements();
                        calculationStack.push(Operation.NULL.name());
                    }
                    displayStringObserver.updateString("0");
                    break;
                case R.id.changeSizeButton:
                    if (displayString.charAt(0) == '-') {
                        displayStringObserver.updateString(displayString.substring(1));
                    } else {
                        displayStringObserver.updateString("-".concat(displayString));
                    }
                    break;
                case R.id.percentageButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(textValue/100));
                    break;
                case R.id.squareButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(textValue*textValue));
                    break;
                case R.id.cubeButton:
                    displayStringObserver.updateString(
                            CalculatorUtilities.convertDoubleToString(textValue*textValue*textValue));
                    break;
                case R.id.inverseButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(1/textValue));
                    break;
                case R.id.squareRootButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.sqrt(textValue)));
                    break;
                case R.id.cubeRootButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.cbrt(textValue)));
                    break;
                case R.id.logButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.log10(textValue)));
                    break;
                case R.id.naturalLogButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.log(textValue)));
                    break;
                case R.id.epowButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.pow(Math.E, textValue)));
                    break;
                case R.id.tenPowButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.pow(10.0, textValue)));
                    break;
                case R.id.factorialButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(
                            CalculatorUtilities.factorial(textValue)));
                    break;
                case R.id.sinButton:
                case R.id.cosButton:
                case R.id.tanButton:
                case R.id.sinhButton:
                case R.id.coshButton:
                case R.id.tanhButton:
                    handleTrigonometryOperation(button, textValue);
                    break;
                case R.id.randButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.random()));
                    break;
                case R.id.eButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.E));
                    break;
                case R.id.pieButton:
                    displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(Math.PI));
                    break;
                case R.id.moreOptionButton:
                    handleMoreOptionButton();
                    break;
                case R.id.radianButton:
                    handleRadianButton();
                case R.id.openBracketButton:
                    if (!lastOperation.equals(OperationType.BINARY) && !calculationStack.peek().equals(Operation.NULL.name())) {
                        applyOperation(Operation.MULTIPLICATION);
                    }
                    calculationStack.push(Operation.OPENBRACKET.name());
                    calculationStack.push("0");
                    calculationStack.push(Operation.ADDITION.name());
                    displayStringObserver.updateString("0", false);
                    break;
                case R.id.closeBracketButton:
                    applyCloseBracketOperation();
                    break;
            }
        }
        lastOperation = OperationType.UNARY;
    }

    private void applyEqualOperation() {
        Operation prevOperation = Operation.valueOf(calculationStack.peek());
        Double currentValue = CalculatorUtilities.convertStringToDouble(displayStringObserver.getObservedString());
        while (!calculationStack.peek().equals(Operation.NULL.name())) {
            calculationStack.pop();
            if (prevOperation.equals(Operation.OPENBRACKET)) {
                continue;
            }
            double prevValue = CalculatorUtilities.convertStringToDouble(calculationStack.pop());
            currentValue = CalculatorUtilities.applyOperation(prevOperation,prevValue,currentValue);
            prevOperation = Operation.valueOf(calculationStack.peek());
        }
        displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(currentValue));
    }

    private void applyCloseBracketOperation() {
        Operation prevOperation = Operation.valueOf(calculationStack.peek());
        Double currentValue = CalculatorUtilities.convertStringToDouble(displayStringObserver.getObservedString());
        while (!prevOperation.equals(Operation.NULL) && !prevOperation.equals(Operation.OPENBRACKET)) {
            calculationStack.pop();
            double prevValue = CalculatorUtilities.convertStringToDouble(calculationStack.pop());
            currentValue = CalculatorUtilities.applyOperation(prevOperation,prevValue,currentValue);
            prevOperation = Operation.valueOf(calculationStack.peek());
        }
        displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(currentValue));
        if (prevOperation.equals(Operation.OPENBRACKET)) {
            calculationStack.pop();
        }
    }

    private void applyOperation(Operation operation) {
        Operation currentOperation = Operation.valueOf(calculationStack.peek());
        if (currentOperation.isLowerPrecedence(operation)) {
            calculationStack.push(displayStringObserver.getObservedString());
            if (!operation.equals(Operation.NULL)) {
                displayStringObserver.updateString("0", false);
                calculationStack.push(operation.name());
            } else {
                calculationStack.removeAllElements();
                calculationStack.push(Operation.NULL.name());
            }
        } else {
            double displayValue = CalculatorUtilities.convertStringToDouble(displayStringObserver.getObservedString());
            Operation previousOperation = Operation.valueOf(calculationStack.pop());
            double previousValue = CalculatorUtilities.convertStringToDouble(calculationStack.pop());
            String newValue = CalculatorUtilities.applyOperationAndGetString(previousOperation, previousValue, displayValue);
            displayStringObserver.updateString(newValue);
            applyOperation(operation);
        }
    }

    private void handleTrigonometryOperation(Button button, double textValue) {
        int buttonId = button.getId();
        if (!isRadian && !isMoreOperation) {
            textValue = textValue * Math.PI / 180;
        }
        double newValue;
        switch (buttonId) {
            case R.id.sinButton:
                newValue = isMoreOperation ? Math.asin(textValue) : Math.sin(textValue);
                break;
            case R.id.cosButton:
                newValue = isMoreOperation ? Math.acos(textValue) : Math.cos(textValue);
                break;
            case R.id.tanButton:
                newValue = isMoreOperation ? Math.atan(textValue) : Math.tan(textValue);
                break;
            case R.id.sinhButton:
                newValue = isMoreOperation ? CalculatorUtilities.asinh(textValue) : Math.sinh(textValue);
                break;
            case R.id.coshButton:
                newValue = isMoreOperation ? CalculatorUtilities.acosh(textValue) : Math.cosh(textValue);
                break;
            case R.id.tanhButton:
                newValue = isMoreOperation ? CalculatorUtilities.atanh(textValue) : Math.tanh(textValue);
                break;
            default:
                return;
        }
        if (!isRadian && isMoreOperation) {
            newValue = newValue * 180 / Math.PI;
        }
        displayStringObserver.updateString(CalculatorUtilities.convertDoubleToString(newValue));
    }

    private void handleMoreOptionButton() {
        int[] trigonoButtonIdArrray = {
                R.id.sinButton,
                R.id.cosButton,
                R.id.tanButton,
                R.id.sinhButton,
                R.id.coshButton,
                R.id.tanhButton
        };
        String[] trigonoButtonStringArrray = {
                "sin",
                "cos",
                "tan",
                "sinh",
                "cosh",
                "tanh",
        };
        for (int i = 0; i<6; i++) {
            Button button = (Button) findViewById(trigonoButtonIdArrray[i]);
            if (!isMoreOperation) {
                button.setText(trigonoButtonStringArrray[i] + "⁻¹");
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            } else {
                button.setText(trigonoButtonStringArrray[i]);
            }
        }
        isMoreOperation = !isMoreOperation;
    }

    private void handleRadianButton() {
        Button radianButton = (Button) findViewById(R.id.radianButton);
        if (isRadian) {
            radianButton.setText("Rad");
        } else {
            radianButton.setText("Deg");
        }
        isRadian = !isRadian;
    }

    private void clearSingleDigit() {
        String displayString = displayStringObserver.getObservedString();
        int displayStringLen = displayString.length();
        if (displayString.length() == 1 ||
                (displayString.charAt(0) == '-' && displayStringLen == 2)) {
            displayStringObserver.updateString("0");
        } else {
            displayStringObserver.updateString(displayString.substring(0, displayStringLen - 1));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("temporaryString", temporaryString);
        outState.putSerializable("temporaryStack", temporaryStack);
        outState.putString("lastOperation", lastOperation.name());
        outState.putSerializable("calculationStack", calculationStack);
        outState.putString("displayString", displayStringObserver.getObservedString());
        outState.putString("textViewString", ((TextView) findViewById(R.id.textView)).getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        temporaryString = savedInstanceState.getString("temporaryString");
        temporaryStack = (Stack<String>) savedInstanceState.getSerializable("temporaryStack");
        lastOperation = OperationType.valueOf(savedInstanceState.getString("lastOperation"));
        calculationStack = (Stack<String>) savedInstanceState.getSerializable("calculationStack");
        displayStringObserver.updateString(savedInstanceState.getString("displayString"), false);
        String textViewString = savedInstanceState.getString("textViewString");
        ((TextView) findViewById(R.id.textView)).setText(textViewString);
        ((Button) findViewById(R.id.clearButton)).setText(textViewString.equals("0") ? "AC" : "C");
    }
}
