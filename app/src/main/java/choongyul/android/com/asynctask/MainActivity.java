package choongyul.android.com.asynctask;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * assets 폴더안에 어떤 파일이 필요함. FILE_NAME 에 집어넣어야합니다.
 */
public class MainActivity extends AppCompatActivity {

    // 핸들이 메시지에 담겨오는 what에 대한 정의
    public static final int SET_TEXT = 100;

    final static String FILE_NAME= "abc.MOV";

    boolean flag = false;
    Button btnStart, btnStop, btnDelete;
    TextView result;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = (TextView) findViewById(R.id.tvResult);
        btnStart = (Button) findViewById(R.id.btnStart);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag) {
                    Toast.makeText(MainActivity.this, "실행중입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    flag = true;
                    progressBar.setProgress(0);
                    String filename = FILE_NAME;
                    new TestAsync().execute(filename);
                }
            }
        });
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopProgram();
            }
        });
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delFile(FILE_NAME);
            }
        });
    }


    public void stopProgram() {
        flag = false;
    }

    public void delFile(String filename) {
        String FillPath = getFullPath(filename);
        File file = new File(FillPath);

        if( file.exists()) {
            file.delete();
        }
    }

    public class TestAsync extends AsyncTask<String, Integer, Boolean> {
        // Void의 의미
        // 첫번째 : do in background 메소드의 파라미터 타입. 입력은 execute에서 한다.
        // 두번째 : onProgressUpdate 의 파라미터
        // 세번째 : doInBackGround의 리턴타입

        int totalSize = 0;
        @Override // AsyncTask를 준비하는 메소드
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);

            // 파일 사이즈 입력하기
            AssetManager manager = getAssets();
            try {
                InputStream is = manager.open(FILE_NAME);
                int filesize = is.available(); // stream에 연결된 파일사이즈를 리턴해준다.

                //프로그래스바의 최대값에 파일 사이즈 입력
                progressBar.setMax(filesize);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override // mainThread 에서 실행되는 함수
        protected void onProgressUpdate(Integer... values) {
            int size = values[0];
            // 넘어온 파일 길이를 totalSize에 뿌려준다.
            totalSize = totalSize + size;
            result.setText(totalSize + "");
            progressBar.setProgress(totalSize);
        }

        @Override   // run 과 동일한 함수
        protected Boolean doInBackground(String... params) {

//            String first = params[0]; // execute 에서 넘어온 첫번째 값이다. "안녕하세요"
//            String second = params[1]; // execute에서 넘어온 두번째 값이다. "1111"

            String filename = params[0];

            assetToDisk(filename);
            return true;
        }

        // doInBackGround가 종료된 후에 리턴값을 받아서 호출되는 함수
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                result.setText("완료되었습니다");
            }
        }


        // asset에 있는 파일을 쓰기 가능한 internal Storage로 복사한다.
        // Internal storage의 경로 구조는  /data/data/패키지명명
        public void assetToDisk(String filename) { // filename : 경로 + 파일이름

            // 스트림 선언
            // try문 안쪽에 선언을 하게되면 exception 발생시 close함수를 호출할 방법이 없다. 그래서 밖에 선언한다.
            InputStream is = null;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            try {
                // 1. assets 에 있는 파일을 filename으로 읽어온다.
                AssetManager manager = getAssets();
                // 2. 파일 스트림 생성
                is = manager.open(filename);

                // 3. 버퍼 스트림으로 래핑(한번에 여러개의 데이터를 가져오기 위한 래핑)
                bis = new BufferedInputStream(is);

                // 쓰기위한 준비작업업
                // 4. 저장할 위치에 파일이 없으면 생성.
                String targetFile = getFullPath(filename);
                File file = new File(targetFile);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 5. 쓰기 스트림을 생성
                fos = new FileOutputStream(file);

                // 6. 버퍼 스트림으로 동시에 여러개의 데이터를 쓰기위한 래핑
                bos = new BufferedOutputStream(fos);

                // 읽어올 데이터를 담아둘 변수
                int read = -1; // 모두 읽어오면 -1이 저장된다.

                // 한번에 읽을 버퍼의 크기를 지정
                byte buffer[] = new byte[1024];
                // 읽어올 데이터가 없을 때 까지 반복문을 돌면서 읽고 쓴다.
                while ((read = bis.read(buffer, 0, 1024)) != -1) {
                    bos.write(buffer, 0, read);

                    // AsyncTask 의 onProgressbar
                    publishProgress(read);
                }
                // 남아있는 데이터를 다 흘려보낸다.
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { // 사용한 순서 역순으로 닫아준다. 제일먼저 사용한걸 제일 나중에 닫아줌.
                    // 물론 스트럼만 닫아도 닫히긴 한다.
                    if (bos != null) bos.close();
                    if (fos != null) fos.close();
                    if (bis != null) bis.close();
                    if (is != null) is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 파일의 전체 경로를 만들어주는 함수
        private String getFullPath(String filename) {
            // /data/data/패키지명/files + / + 파일명  을 리턴한다.
            return getFilesDir().getAbsolutePath() + File.separator + filename;
        }
    }
    private String getFullPath(String filename) {
        // /data/data/패키지명/files + / + 파일명  을 리턴한다.
        return getFilesDir().getAbsolutePath() + File.separator + filename;
    }
}
