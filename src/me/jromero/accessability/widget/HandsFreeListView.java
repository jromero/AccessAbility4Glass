package me.jromero.accessability.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class HandsFreeListView extends GravityListView {

    protected static final String TAG = HandsFreeListView.class.getSimpleName();

    private static final Pattern PATTERN_NUMBERS = Pattern.compile("[0-9]+");

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mRecognizerIntent;

    public HandsFreeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HandsFreeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HandsFreeListView(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSpeechRecognition();
    }

    @Override
    public void onResume() {
        super.onResume();
        startRecognition();
    }

    @Override
    public void onPause() {
        stopRecognition();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        stopRecognition();
        destroyRecognition();
        super.onDestroy();
    }

    private boolean initSpeechRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(getContext())) {
            mSpeechRecognizer = SpeechRecognizer
                    .createSpeechRecognizer(getContext());
            mSpeechRecognizer.setRecognitionListener(mRecognitionListener);

            mRecognizerIntent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    getContext().getPackageName());
            mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mRecognizerIntent
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

            return true;
        } else {
            Log.e(TAG, "Speech Recognition not available on this device.");
            return false;
        }
    }

    private void startRecognition() {
        if (mSpeechRecognizer != null) {
            Log.i(TAG, "startRecognition");
            mSpeechRecognizer.startListening(mRecognizerIntent);
        } else {
            Log.w(TAG, "startRecognition: Recognizer null!");
        }
    }

    private void stopRecognition() {
        if (mSpeechRecognizer != null) {
            Log.i(TAG, "stopRecognition");
            mSpeechRecognizer.stopListening();
        } else {
            Log.w(TAG, "stopRecognition: Recognizer null!");
        }
    }

    private void destroyRecognition() {
        if (mSpeechRecognizer != null) {
            Log.i(TAG, "destroyRecognition");
            mSpeechRecognizer.destroy();
        } else {
            Log.w(TAG, "destroyRecognition: Recognizer null!");
        }
    }


    private void processRecognitionResults(List<String> resultsList) {
        String resultString = TextUtils.join(",", resultsList);
        for (String result : resultsList) {
            if (TextUtils.isEmpty(result)) {
                continue;
            }

            for (SpeechEvent speechEvent : SpeechEvent.values()) {
                String[] phrases = TextUtils.split(speechEvent.getPhrases(), ",");
                for (String phrase : phrases) {
                    if (result.contains(phrase)) {
                        Log.i(TAG, "Matched event: " + speechEvent);

                        if (speechEvent.isMultipliable()) {
                            /**
                             * we search the entire set since it could look
                             * something like this:
                             * down to,down 2,down too,down two,down to earth
                             */
                            Matcher matcher = PATTERN_NUMBERS.matcher(resultString);

                            // we only care about the first one that's why it's
                            // not a while loop
                            if (matcher.find()) {
                                String multiplier = matcher.group();
                                mSpeechEventListener.onSpeechEvent(speechEvent,
                                        Integer.parseInt(multiplier));
                            } else {
                                mSpeechEventListener.onSpeechEvent(speechEvent, 1);
                            }
                        } else {
                            mSpeechEventListener.onSpeechEvent(speechEvent, 1);
                        }

                        return;
                    }
                }
            }
        }

        mRecognitionListener.onError(SpeechRecognizer.ERROR_NO_MATCH);
    }

    private RecognitionListener mRecognitionListener = new RecognitionListener() {

        private static final boolean DEBUG = false;

        @Override
        public void onRmsChanged(float rmsdB) {
            if (DEBUG) Log.v(TAG, "onRmsChanged");
        }

        @Override
        public void onResults(Bundle results) {
            if (DEBUG) Log.v(TAG, "onResults");

            List<String> resultsList = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);

            // Some devices return null (although they shouldn't)
            if (resultsList == null) {
                onError(SpeechRecognizer.ERROR_NO_MATCH);
                return;
            }

            Log.i(TAG, "Recognition results: " + TextUtils.join(",", resultsList));
            processRecognitionResults(resultsList);
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            if (DEBUG) Log.v(TAG, "onReadyForSpeech");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            if (DEBUG) Log.v(TAG, "onPartialResults");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            if (DEBUG) Log.v(TAG, "onEvent");
        }

        @Override
        public void onError(int error) {
            if (DEBUG) Log.v(TAG, "onError");

            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";

                    // TODO: Error out
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";

                    // TODO: Error out
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";

                    // TODO: Error out
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";

                    // restart recognition
                    startRecognition();
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network timeout";

                    // restart recognition
                    startRecognition();
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "No match";

                    // restart recognition
                    startRecognition();
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";

                    // TODO: Error out
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "Error from server";

                    // restart recognition
                    startRecognition();
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "Speech timeout";

                    // restart recognition
                    startRecognition();
                    break;
                default:
                    message = "Unknown error";

                    // TODO: Error out
                    break;
            }

            Log.e(TAG, "Error: code: " + error + " message: " + message);
        }

        @Override
        public void onEndOfSpeech() {
            if (DEBUG) Log.v(TAG, "onEndOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            if (DEBUG) Log.v(TAG, "onBufferReceived");
        }

        @Override
        public void onBeginningOfSpeech() {
            if (DEBUG) Log.v(TAG, "onBeginningOfSpeech");
        }
    };

    private SpeechEventListener mSpeechEventListener = new SpeechEventListener() {

        @Override
        public void onSpeechEvent(SpeechEvent event, int multiplier) {
            Log.i(TAG, "Event: " + event);
            Log.i(TAG, "Multiplier: " + multiplier);

            switch (event) {
                case DOWN:
                    selectNextDown(multiplier);
                    break;
                case UP:
                    selectNextUp(multiplier);
                    break;
                default:
                    break;
            }
            // restart recognition
            startRecognition();
        }

        private void selectNextUp(int times) {
            int selectedItemPosition = getSelectedItemPosition();
            View selectedView = getViewAtPosition(selectedItemPosition);
            int toBeSelected = selectedItemPosition - times;
            if (toBeSelected <= 0) {
                toBeSelected = 0;
            }

            if (selectedView != null) {
                setSelectionFromTop(toBeSelected, selectedView.getHeight());
            } else {
                setSelection(toBeSelected);
            }
        }

        private void selectNextDown(int times) {
            int selectedItemPosition = getSelectedItemPosition();
            View selectedView = getViewAtPosition(selectedItemPosition);
            int toBeSelected = selectedItemPosition + times;
            if (toBeSelected >= getCount()) {
                toBeSelected = getCount() - 1;
            }

            if (selectedView != null) {
                setSelectionFromTop(toBeSelected, selectedView.getHeight());
            } else {
                setSelection(toBeSelected);
            }
        }
    };

    public static interface SpeechEventListener {
        void onSpeechEvent(SpeechEvent event, int multiplier);
    }

    public static enum SpeechEvent {
        SELECT("click,select"),
        UP("up"),
        DOWN("down"),
        BACK("back"),
        NEXT("next");

        private String mPhrases;

        SpeechEvent(String phrases) {
            mPhrases = phrases;
        }

        public String getPhrases() {
            return mPhrases;
        }

        public boolean isMultipliable() {
            switch (this) {
                case UP:
                case DOWN:
                    return true;
                default:
                    return false;
            }
        }
    }
}
