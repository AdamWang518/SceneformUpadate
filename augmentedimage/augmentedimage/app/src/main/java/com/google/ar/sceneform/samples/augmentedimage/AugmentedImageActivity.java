/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.samples.ModelAction.DepartmentAdapter;
import com.google.ar.sceneform.samples.ModelAction.buildingAdapter;
import com.google.ar.sceneform.samples.Models.buildingModel;
import com.google.ar.sceneform.samples.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * ArAugmentedImage_getTrackingMethod() and render only when the tracking method equals to
 * AR_AUGMENTED_IMAGE_TRACKING_METHOD_FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/c/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public  class AugmentedImageActivity extends AppCompatActivity {

  private ArFragment arFragment;
  private int SPEECH_REQUEST_CODE = 0;
  private boolean isImageDetected=false;
  private AlertDialog show;
  // Augmented image and its associated center pose anchor, keyed by the augmented image in
  // the database.
  private final HashMap<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();
  Button btn = null;
  Scene scene = null;
  RequestQueue mQueue =null;
  AugmentedImageNode node = null;
  AugmentedImage image = null;
  View voiceView = null;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.mQueue = Volley.newRequestQueue(this);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    scene = arFragment.getArSceneView().getScene();
    node = new AugmentedImageNode(this);

    scene.addOnUpdateListener(this::onUpdateFrame);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (augmentedImageMap.isEmpty()) {

      Log.d("leolog","OnResume");
    }
  }

  /**
   * Registered with the Sceneform Scene object, this method is called at the start of each frame.
   *
   * @param frameTime - time since last frame.
   */
//  private void onUpdateFrame(FrameTime frameTime) {
//    Collection<AugmentedImage> updatedAugmentedImages =
//            arFragment.getArSceneView().getSession().getAllTrackables(AugmentedImage.class);
//
//    for (AugmentedImage face : updatedAugmentedImages) {
//      if (!augmentedImageMap.containsKey(face)) {
//        AugmentedImageNode faceNode = new AugmentedImageNode(this);
//        faceNode.setParent(scene);
//        augmentedImageMap.put(face, faceNode);
//        arFragment.getArSceneView().getScene().addChild(node);
//      }
//    }
//
//    // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
//    Iterator<Map.Entry<AugmentedImage, AugmentedImageNode>> iter =
//            augmentedImageMap.entrySet().iterator();
//    while (iter.hasNext()) {
//      Map.Entry<AugmentedImage, AugmentedImageNode> entry = iter.next();
//      AugmentedImage face = entry.getKey();
//      if (face.getTrackingState() == TrackingState.STOPPED) {
//        AugmentedImageNode faceNode = entry.getValue();
//        faceNode.setParent(null);
//        iter.remove();
//      }
//    }
//
//  }


//
//
    private void onUpdateFrame(FrameTime frameTime) {//反覆被呼叫
      Log.d("leolog","Detecting");
      if(isImageDetected) {
        return;
      }
    Frame frame = arFragment.getArSceneView().getArFrame();
    Log.d("leolog","OnUpdateFrame");
    // If there is no frame, just return.
    if (frame == null) {
      return;
    }

    Collection<AugmentedImage> updatedAugmentedImages =
        frame.getUpdatedTrackables(AugmentedImage.class);
    for (AugmentedImage augmentedImage : updatedAugmentedImages) {
      SnackbarHelper.getInstance().showMessage(this, augmentedImage.getTrackingState().toString());
      //Log.d("leolog",augmentedImage.getTrackingState().toString());
      SnackbarHelper.getInstance().showMessage(this, augmentedImage.getTrackingState().toString());
      switch (augmentedImage.getTrackingState()) {
        case PAUSED:
          // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
          // but not yet tracked.
          String text = "Detected Image: " + augmentedImage.getName();
          SnackbarHelper.getInstance().showMessage(this, text);
          break;

        case TRACKING:
          // Have to switch to UI Thread to update View.

         //搞懂如何反覆投放
          if (!augmentedImageMap.containsKey(augmentedImage)) {
            //node = new AugmentedImageNode(this);
            this.image = augmentedImage;

            node.setImage(this.image,augmentedImage.getName());
            augmentedImageMap.put(this.image, node);
            arFragment.getArSceneView().getScene().addChild(node);
            isImageDetected = true;
          }
          break;

        case STOPPED:
          augmentedImageMap.remove(this.image);
          break;
      }
    }
  }


  public void clearDetect(View view) throws InterruptedException {

    Collection<Anchor> anchors = arFragment.getArSceneView().getSession().getAllAnchors();

    for(Anchor anchor : anchors) {
      anchor.detach();
      Log.d("leolog2",anchor.getTrackingState().toString());
    }

    Iterator<Map.Entry<AugmentedImage, AugmentedImageNode>> iter =
            augmentedImageMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<AugmentedImage, AugmentedImageNode> entry = iter.next();
      AugmentedImageNode faceNode = entry.getValue();
      faceNode.setParent(null);
     
      iter.remove();
      augmentedImageMap.remove(this.image);
      arFragment.getArSceneView().getScene().removeChild(faceNode);
    }


    arFragment.getArSceneView().getScene().removeChild(this.node);
    augmentedImageMap.clear();

    this.isImageDetected  = false;

//////////cancel////////////
//    Collection<Anchor> anchors = arFragment.getArSceneView().getSession().getAllAnchors();
//
//    for(Anchor anchor : anchors) {
//      anchor.detach();
//      Log.d("leolog2",anchor.getTrackingState().toString());
//    }
//
//    fitToScanView.setVisibility(View.VISIBLE);
//
//    Iterator<Map.Entry<AugmentedImage, AugmentedImageNode>> iter =
//            augmentedImageMap.entrySet().iterator();
//    while (iter.hasNext()) {
//      Map.Entry<AugmentedImage, AugmentedImageNode> entry = iter.next();
//      AugmentedImage face = entry.getKey();
//      AugmentedImageNode faceNode = entry.getValue();
//      faceNode.setParent(null);
//      iter.remove();
//      augmentedImageMap.remove(this.image);
//
//    }
////////////// cancel end////////
    // augmentedImageMap.remove(this.image);  => 用來判定物件生成的所在位置的圖片，若把圖片清空的話，物件就無法被放入，可以從這邊觀察下手，想辦法讓他能重新識別
  // 想辦法如何再把其他圖片放入，讓物件能重新擺上去
//    fitToScanView.setVisibility(View.VISIBLE);
//
//    Iterator<Map.Entry<AugmentedImage, AugmentedImageNode>> iter =
//            augmentedImageMap.entrySet().iterator();
//    while (iter.hasNext()) {
//      Map.Entry<AugmentedImage, AugmentedImageNode> entry = iter.next();
//      AugmentedImage face = entry.getKey();
//      AugmentedImageNode faceNode = entry.getValue();
//      faceNode.setParent(null);
//      iter.remove();
//      augmentedImageMap.remove(this.image);
//
//    }
//
//

//    this.node.getAnchor().detach();
//    this.node.setParent(null);
//    this.node = null;

    //arFragment.getArSceneView().getScene().removeChild(node);
  }

  public void Put(View view) {
    arFragment.getArSceneView().getScene().addChild(node);
  }

  public void listen(View view) {

    displaySpeechRecognizer();
  }
  private void displaySpeechRecognizer() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    startActivityForResult(intent, SPEECH_REQUEST_CODE);
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    try {
      if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
        List<String> results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);
        String spokenText = results.get(0);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        voiceView = View.inflate(this, R.layout.listencheck, null);
        EditText editText = voiceView.findViewById(R.id.listenText);
        editText.setText(spokenText);
        alert.setView(voiceView);
        show=alert.show();
        //Toast.makeText(this,spokenText,Toast.LENGTH_SHORT).show();

      }
    }
    catch (Exception exception){
      Log.d("explode",exception.getMessage());
    }

  }

    public void restart(View view) {
      Intent intent = getIntent();
      finish();
      startActivity(intent);
    }

  public void send(View view) {
    show.dismiss();
    String speak=null;
    String url = this.getResources().getString(R.string.url);
    //View sendview = View.inflate(this, R.layout.listencheck, null);
    EditText editText = voiceView.findViewById(R.id.listenText);
    speak=editText.getText().toString();
    Log.d("speaked",speak);
    AugmentedImageActivity self = this;
    try {
      JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url + String.format("getVoice?text=%s", speak), new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray jsonArray) {
          ArrayList<buildingModel> buildingList=new ArrayList<buildingModel>();
          Log.d("arraylog",String.valueOf(jsonArray));
          for (int i=0;i< jsonArray.length();i++)
          {
            try {
              JSONObject json= jsonArray.getJSONObject(i);
              int Floor=json.getInt("Floor");
              int Similarity=json.getInt("Similarity");
              String Department=json.getString("Department");
              String BuildingName=json.getString("BuildingName");
              buildingModel model = new buildingModel(Similarity,Floor,Department,BuildingName);
              Log.d("Building",Similarity+" "+Floor+" "+Department+" "+BuildingName);
              buildingList.add(model);
            } catch (JSONException e) {
              e.printStackTrace();
              Log.d("Building",e.getMessage());
            }
          }
          try {
            self.node.initNavigation(buildingList);
            //設定文字
          }
          catch (Exception exception)
          {
            Log.d("explode",exception.getMessage());
          }

        }

      }, null);
      this.mQueue.add(request);

    }
    catch (Exception exception){
      Log.d("explode",exception.getMessage());
    }


  }
}
