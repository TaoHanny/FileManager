package com.install.arc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.VersionInfo;
import com.install.arc.utils.ImageUtil;
import com.install.arc.utils.TrackUtil;
import com.instwall.base.data.DetectFace;
import com.instwall.base.utils.SharedUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ashy.earl.common.app.App;
import ashy.earl.common.closure.EarlCall;
import ashy.earl.common.closure.Method0_0;
import ashy.earl.common.closure.Method2_0;
import ashy.earl.common.closure.Params0;
import ashy.earl.common.closure.Params2;
import ashy.earl.common.task.RarTask;
import ashy.earl.common.task.Task;
import ashy.earl.magicshell.clientapi.RuntimeModule;

import static ashy.earl.common.closure.Earl.bind;

public class ArcClient {
    private static final String TAG = "ArcClient";
    public static final String IMG_SUFFIX = ".jpg";
    ///sdcard/arcFace/register
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "arcFace";

    public static final String REGISTER_DIR = ROOT_DIR + File.separator + "register" + File.separator;
    public static final String REGISTER_FAILED_DIR = ROOT_DIR + File.separator + "failed";

    private static final String SAVE_FEATURE_DIR = "register" + File.separator + "features";
    private static final String SAVE_IMG_DIR = "register" + File.separator + "images";
    private static final String APP_ID = "HgnvQV7BD7X4DDCkHXD1SBqGmn1duTgDhWG8bsEHPqvw";
    private static final String SDK_KEY = "3HEiMcmyQcctgcL3ahxBhqJdbo3rijTef6dqyfo9ZNdx";
    private int processMask = FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE_RECOGNITION;
    // 需要考虑线程安全问题 同一个线程使用该对象
    private FaceEngine mArcFaceEngine;
    //    private RegisterCore mRegisterCore;
    private int mRotate = FaceEngine.ASF_OP_0_ONLY;
    private static ArcClient sSelf;
    // for sp
    private SharedUtil mSp;

    public static ArcClient get(Context mContext) {
        if (sSelf != null) return sSelf;
        synchronized (ArcClient.class) {
            if (sSelf == null) sSelf = new ArcClient(mContext);
        }
        return sSelf;
    }

    public void releaseEngineSdk() {
        if (mArcFaceEngine != null) {
            int code = mArcFaceEngine.unInit();
            Log.d(TAG, "ASAE_FSDK_releaseEngineSdk code=" + code);
            mArcFaceEngine = null;
        }
    }

    private ArcClient(Context context) {
        mSp = new SharedUtil(context);
        boolean isActive = mSp.readArcEngineState();
        Log.d(TAG, "~%s ArcClient isActive=" + isActive);
        if (isActive) {
            initEngine();
            return;
        }
        if (mArcFaceEngine == null) {
            mArcFaceEngine = new FaceEngine();
        }
        App.getBgLoop().postTask(new RarTask(bind(activeArcEngine, ArcClient.this),
                bind(obtainInitEngineCode, ArcClient.this)));

    }

    @EarlCall
    private Integer activeArcEngine() {
        return mArcFaceEngine.active(App.getAppContext(), APP_ID, SDK_KEY);

    }

    @EarlCall
    private void obtainInitEngineCode(Integer code, RuntimeException ex) {
        if (code == ErrorInfo.MOK || code == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            // do save active state
            mSp.saveArcEngineState(true);
            initEngine();
            return;
        }
        mArcFaceEngine = null;
        Log.d(TAG, "~ ASAE_FSDK_obtainInitEngine error Code = " + code);
        if (code == 90129) {
            // fix me for arc init
            RuntimeModule.get().helperSimpleShell("pm clear com.shutuo.face.register");
        }
    }

    @MainThread
    @EarlCall
    private void initEngine() {
        int tempRotate = FaceEngine.ASF_OP_0_ONLY;
        switch (mRotate) {
            case 0:
                tempRotate = FaceEngine.ASF_OP_0_ONLY;
                break;
            case 90:
                tempRotate = FaceEngine.ASF_OP_90_ONLY;
                break;
            case 180:
                tempRotate = FaceEngine.ASF_OP_180_ONLY;
                break;
            case 270:
                tempRotate = FaceEngine.ASF_OP_270_ONLY;
                break;
        }

        if (mArcFaceEngine == null) {
            mArcFaceEngine = new FaceEngine();
        }
        int faceEngineCode = mArcFaceEngine.init(App.getAppContext(), FaceEngine.ASF_DETECT_MODE_VIDEO, tempRotate,
                16, 10, processMask);
        VersionInfo versionInfo = new VersionInfo();
        mArcFaceEngine.getVersion(versionInfo);
        Log.e(TAG, " AFT_FSDK_InitialFaceEngine = " + faceEngineCode + " version info=  " + versionInfo.toString());
        synchronized (ArcClient.this) {
            mTaskInitEngine = null;
        }
        if (faceEngineCode != ErrorInfo.MOK) {
            mArcFaceEngine = null;
        }
    }


    private int currentTrackId;
    private List<Integer> formerTrackIdList = new ArrayList<>();
    private List<Integer> currentTrackIdList = new ArrayList<>();
    private List<Rect> formerFaceRectList = new ArrayList<>();
    private SparseArray<DetectFace> facePreviewInfoList = new SparseArray<>();
    private ConcurrentHashMap<Integer, String> nameMap = new ConcurrentHashMap<>();
    private static final float SIMILARITY_RECT = 0.3f;
    private List<FaceInfo> faceInfoList = new ArrayList<>();
    private List<DetectFace> mLastFaceList = new ArrayList<>();
    private Task mTaskInitEngine;

    @WorkerThread
    public ArcData trackFace(byte[] data, int oW, int oH) {
        if (mArcFaceEngine == null) {
            Log.i(TAG, " trackFace Engine state error ");
            // fixme for engine retry init engine
            synchronized (ArcClient.this) {
                if (mTaskInitEngine != null) {
                    return null;
                }

            }
            mTaskInitEngine = bind(initEngine, ArcClient.this).task();
            App.getMainLoop().postTask(mTaskInitEngine);
            return null;
        }
        faceInfoList.clear();
        int code = mArcFaceEngine.detectFaces(data, oW, oH, FaceEngine.CP_PAF_NV21, faceInfoList);
        if (code != ErrorInfo.MOK) {
            Log.i(TAG, " trackFace detectFaces error code=" + code);
            return null;
        }
        ArcData tempData = new ArcData();
        refreshTrackId(faceInfoList);
        Log.i(TAG, " trackFace detectFaces newFace Count=" + newCount);
        tempData.mCount = newCount;
        facePreviewInfoList.clear();
        if (faceInfoList.size() > 0) {
            // step1 get new face list
            // step2 get data cache
            // step3 compare face track
            if (mLastFaceList.isEmpty()) {
                code = mArcFaceEngine.process(data, oW, oH, FaceEngine.CP_PAF_NV21, faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER);
                if (code == ErrorInfo.MOK) {
                    List<AgeInfo> ageInfoList = new ArrayList<>();
                    List<GenderInfo> genderInfoList = new ArrayList<>();
                    int ageCode = mArcFaceEngine.getAge(ageInfoList);
                    int genderCode = mArcFaceEngine.getGender(genderInfoList);
                    //有其中一个的错误码不为0，return
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        FaceInfo arcFace = faceInfoList.get(i);
                        DetectFace tempFace = new DetectFace();
                        tempFace.mTackID = currentTrackIdList.get(i);
                        tempFace.mRect = arcFace.getRect();
                        int age = ageInfoList.get(i).getAge();
                        int gender = genderInfoList.get(i).getGender();
                        String tempGender = null;
                        if (gender == 1) {
                            tempGender = "female";
                        } else if (gender == 0) {
                            tempGender = "male";
                        }

                        Log.d(TAG, " AFD_FSDK  Arc  gender=  " + tempGender + " age= " + age);
                        if (!TextUtils.isEmpty(tempGender)) {
                            tempFace.mAttribute = new DetectFace.Attribute(tempGender, -1, age, false,
                                    false, false, false, false, -1, false, null);

                            mLastFaceList.add(tempFace);
                            facePreviewInfoList.append(currentTrackIdList.get(i), tempFace);
                        }

                    }
                } else {
                    Log.d(TAG, "AFD_FSDK just Detect data  process error code =  " + code);
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        FaceInfo arcFace = faceInfoList.get(i);
                        DetectFace tempFace = new DetectFace();
                        tempFace.mTackID = currentTrackIdList.get(i);
                        tempFace.mRect = arcFace.getRect();
                        facePreviewInfoList.append(currentTrackIdList.get(i), tempFace);
                    }
                }

            } else {
                int oldCount = 0;
                for (int l = 0; l < faceInfoList.size(); l++) {
                    FaceInfo arcFace = faceInfoList.get(l);
                    DetectFace tempFace = new DetectFace();
                    tempFace.mTackID = currentTrackIdList.get(l);
                    tempFace.mRect = arcFace.getRect();
                    facePreviewInfoList.append(currentTrackIdList.get(l), tempFace);
                }

                for (int m = 0; m < mLastFaceList.size(); m++) {
                    DetectFace face = mLastFaceList.get(m);
                    for (int k = 0; k < facePreviewInfoList.size(); k++) {
                        DetectFace newFace = facePreviewInfoList.valueAt(k);
                        if (face.mTackID == newFace.mTackID) {
                            facePreviewInfoList.setValueAt(k, face);
                            oldCount += 1;
                        }
                    }
                }

                // need update
                if (oldCount != facePreviewInfoList.size()) {
                    Log.d(TAG, " AFD_FSDK  Arc  need attribute face ");
                    code = mArcFaceEngine.process(data, oW, oH, FaceEngine.CP_PAF_NV21, faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER);
                    if (code == ErrorInfo.MOK) {
                        facePreviewInfoList.clear();
                        mLastFaceList.clear();
                        List<AgeInfo> ageInfoList = new ArrayList<>();
                        List<GenderInfo> genderInfoList = new ArrayList<>();
                        int ageCode = mArcFaceEngine.getAge(ageInfoList);
                        int genderCode = mArcFaceEngine.getGender(genderInfoList);
                        //有其中一个的错误码不为0，return
                        for (int i = 0; i < faceInfoList.size(); i++) {
                            FaceInfo arcFace = faceInfoList.get(i);
                            DetectFace tempFace = new DetectFace();
                            tempFace.mTackID = currentTrackIdList.get(i);
                            tempFace.mRect = arcFace.getRect();
                            int age = ageInfoList.get(i).getAge();
                            int gender = genderInfoList.get(i).getGender();
                            String tempGender = null;
                            if (gender == 1) {
                                tempGender = "female";
                            } else if (gender == 0) {
                                tempGender = "male";
                            }
                            Log.d(TAG, " AFD_FSDK  Arc  gender=  " + tempGender + " age= " + age);
                            if (!TextUtils.isEmpty(tempGender)) {
                                tempFace.mAttribute = new DetectFace.Attribute(tempGender, -1, age, false,
                                        false, false, false, false, -1, false, null);

                                mLastFaceList.add(tempFace);
                                facePreviewInfoList.append(currentTrackIdList.get(i), tempFace);
                            }

                        }
                    } else {
                        Log.d(TAG, " AFD_FSDK  Arc  need attribute face progress error code= " + code);
                    }
                }

            }

        }
        tempData.mArcFaceLst = facePreviewInfoList;
        return tempData;

    }


    /**
     * 注册人脸
     *
     * @param nv21   NV21数据
     * @param width  NV21宽度
     * @param height NV21高度
     * @param name   保存的名字，可为空
     * @return 是否注册成功
     */
    @WorkerThread
    public boolean register(byte[] nv21, int width, int height, String name) {
        if (mArcFaceEngine == null || nv21 == null || width % 4 != 0 || nv21.length != width * height * 3 / 2) {
            return false;
        }

        boolean dirExists = true;
        //存储 人脸特征 文件夹
        File featureDir = new File(ROOT_DIR + File.separator + SAVE_FEATURE_DIR);
        if (!featureDir.exists()) {
            Log.d(TAG, "register  name = [" + name + "]" + "parent feature dir no exit");
            dirExists = featureDir.mkdirs();
        }

        if (!dirExists) {
            return false;
        }
        //存储注册人脸图片 文件夹
        File imgDir = new File(ROOT_DIR + File.separator + SAVE_IMG_DIR);
        if (!imgDir.exists()) {
            dirExists = imgDir.mkdirs();
            Log.d(TAG, "register  name = [" + name + "]" + "parent image  dir no exit");
        }

        if (!dirExists) {
            return false;
        }

        //1. face detect
        List<FaceInfo> faceInfoList = new ArrayList<>();
        int code = mArcFaceEngine.detectFaces(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfoList);
        if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
            FaceFeature faceFeature = new FaceFeature();

            //2. obtain face feature
            code = mArcFaceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeature);
            String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
            try {
                //3.保存注册结果（注册图、特征数据）
                if (code == ErrorInfo.MOK) {
                    YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
                    //为了美观，扩大rect截取注册图
                    Rect cropRect = ImageUtil.getBestRect(width, height, faceInfoList.get(0).getRect());
                    if (cropRect == null) {
                        Log.d(TAG, "register  obtain face rect null");
                        return false;
                    }

                    File file = new File(imgDir + File.separator + userName + IMG_SUFFIX);
                    FileOutputStream fosImage = new FileOutputStream(file);
                    yuvImage.compressToJpeg(cropRect, 100, fosImage);
                    fosImage.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                    //判断人脸旋转角度，若不为0度则旋转注册图
                    boolean needAdjust = false;
                    if (bitmap != null) {
                        switch (faceInfoList.get(0).getOrient()) {
                            case FaceEngine.ASF_OC_0:
                                break;
                            case FaceEngine.ASF_OC_90:
                                bitmap = ImageUtil.getRotateBitmap(bitmap, 90);
                                needAdjust = true;
                                break;
                            case FaceEngine.ASF_OC_180:
                                bitmap = ImageUtil.getRotateBitmap(bitmap, 180);
                                needAdjust = true;
                                break;
                            case FaceEngine.ASF_OC_270:
                                bitmap = ImageUtil.getRotateBitmap(bitmap, 270);
                                needAdjust = true;
                                break;
                            default:
                                break;
                        }
                    }
                    if (needAdjust) {
                        fosImage = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
                        fosImage.close();
                    }
                    // for save face feature
                    FileOutputStream fosFeature = new FileOutputStream(featureDir + File.separator + userName);
                    fosFeature.write(faceFeature.getFeatureData());
                    fosFeature.close();

                    //内存中的数据同步
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;


    }

    public int getFeatureFaceNum() {
        synchronized (this) {
            File featureFileDir = new File(ROOT_DIR + File.separator + SAVE_FEATURE_DIR);
            int featureCount = 0;
            if (featureFileDir.exists() && featureFileDir.isDirectory()) {
                String[] featureFiles = featureFileDir.list();
                featureCount = featureFiles == null ? 0 : featureFiles.length;
            }

            int imageCount = 0;
            File imgFileDir = new File(ROOT_DIR + File.separator + SAVE_IMG_DIR);
            if (imgFileDir.exists() && imgFileDir.isDirectory()) {
                String[] imageFiles = imgFileDir.list();
                imageCount = imageFiles == null ? 0 : imageFiles.length;
            }
            return featureCount > imageCount ? imageCount : featureCount;
        }
    }


    public int clearAllFaceFeatures() {
        synchronized (this) {
            File featureFileDir = new File(ROOT_DIR + File.separator + SAVE_FEATURE_DIR);
            int deletedFeatureCount = 0;
            if (featureFileDir.exists() && featureFileDir.isDirectory()) {
                File[] featureFiles = featureFileDir.listFiles();
                if (featureFiles != null && featureFiles.length > 0) {
                    for (File featureFile : featureFiles) {
                        if (featureFile.delete()) {
                            deletedFeatureCount++;
                        }
                    }
                }
            }
            int deletedImageCount = 0;
            File imgFileDir = new File(ROOT_DIR + File.separator + SAVE_IMG_DIR);
            if (imgFileDir.exists() && imgFileDir.isDirectory()) {
                File[] imgFiles = imgFileDir.listFiles();
                if (imgFiles != null && imgFiles.length > 0) {
                    for (File imgFile : imgFiles) {
                        if (imgFile.delete()) {
                            deletedImageCount++;
                        }
                    }
                }
            }
            return deletedFeatureCount > deletedImageCount ? deletedImageCount : deletedFeatureCount;
        }
    }

    /**
     * 刷新trackId
     *
     * @param ftFaceList 传入的人脸列表
     */
    private int newCount;

    private void refreshTrackId(List<FaceInfo> ftFaceList) {
        currentTrackIdList.clear();
        newCount = 0;
        //每项预先填充-1
        for (int i = 0; i < ftFaceList.size(); i++) {
            currentTrackIdList.add(-1);
        }
        //前一次无人脸现在有人脸，填充新增TrackId
        if (formerTrackIdList.size() == 0) {
            for (int i = 0; i < ftFaceList.size(); i++) {
                currentTrackIdList.set(i, ++currentTrackId);
            }
            newCount = ftFaceList.size();
        } else {
            //前后都有人脸,对于每一个人脸框
            for (int i = 0; i < ftFaceList.size(); i++) {
                //遍历上一次人脸框
                for (int j = 0; j < formerFaceRectList.size(); j++) {
                    //若是同一张人脸
                    if (TrackUtil.isSameFace(SIMILARITY_RECT, formerFaceRectList.get(j), ftFaceList.get(i).getRect())) {
                        //记录ID
                        currentTrackIdList.set(i, formerTrackIdList.get(j));
                        break;
                    }
                }
            }
        }
        //上一次人脸框不存在此人脸，新增
        for (int i = 0; i < currentTrackIdList.size(); i++) {
            if (currentTrackIdList.get(i) == -1) {
                currentTrackIdList.set(i, ++currentTrackId);
                ++newCount;
            }
        }

        formerTrackIdList.clear();
        formerFaceRectList.clear();
        for (int i = 0; i < ftFaceList.size(); i++) {
            formerFaceRectList.add(new Rect(ftFaceList.get(i).getRect()));
            formerTrackIdList.add(currentTrackIdList.get(i));
        }

        //刷新nameMap
//        clearLeftName(currentTrackIdList);
    }


    /**
     * 新增搜索成功的人脸
     *
     * @param trackId 指定的trackId
     * @param name    trackId对应的人脸
     */
    private void addName(int trackId, String name) {
        if (nameMap != null) {
            nameMap.put(trackId, name);
        }
    }

    private String getName(int trackId) {
        return nameMap == null ? null : nameMap.get(trackId);
    }

    /**
     * 清除map中已经离开的人脸
     *
     * @param trackIdList 最新的trackIdList
     */
    private void clearLeftName(List<Integer> trackIdList) {
        Set<Integer> keySet = nameMap.keySet();
        for (Integer integer : keySet) {
            if (!trackIdList.contains(integer)) {
                nameMap.remove(integer);
            }
        }
    }

//    @WorkerThread
//    public boolean doFaceRegister(byte[] nv21, int w, int h, String name) {
//        if (mRegisterCore == null) {
//            return false;
//        }
//        RegisterInfo info = mRegisterCore.register(nv21, w, h, name);
//        if (info == null) {
//            return false;
//        }
//        return true;
//    }
//
//    @WorkerThread
//    public DetectFace doFaceSearch(@NotNull FaceFeature faceFeature, DetectFace face) {
//        if (mRegisterCore == null) {
//            return null;
//        }
//
//        CompareResult compareResult = mRegisterCore.faceSearch(faceFeature);
//        if (compareResult != null) {
//            face.mVipAuth = new DetectFace.VipAuth(compareResult.getUserName(), null, compareResult.getSimilar());
//        }
//        return face;
//    }

    public static class ArcData {
        public SparseArray<DetectFace> mArcFaceLst;
        public int mCount;
    }

    private static final Method0_0<ArcClient, Integer> activeArcEngine
            = new Method0_0<ArcClient, Integer>(ArcClient.class, "activeArcEngine") {
        @Override
        public Integer run(ArcClient target, @NonNull Params0 params) {
            return target.activeArcEngine();
        }
    };
    private static final Method2_0<ArcClient, Void, Integer, RuntimeException> obtainInitEngineCode
            = new Method2_0<ArcClient, Void, Integer, RuntimeException>(ArcClient.class, "obtainInitEngineCode") {
        @Override
        public Void run(ArcClient target, @NonNull Params2<Integer, RuntimeException> params) {
            target.obtainInitEngineCode(params.p1, params.p2);
            return null;
        }
    };
    private static final Method0_0<ArcClient, Void> initEngine
            = new Method0_0<ArcClient, Void>(ArcClient.class, "initEngine") {
        @Override
        public Void run(ArcClient target, @NonNull Params0 params) {
            target.initEngine();
            return null;
        }
    };
}
