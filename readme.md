### 파일 가져오기

파일 가져올때 주소는 /data/data/패키지이름/files + "/" +파일명 으로 가져와야한다. <br/>
파일명 앞에 "/"는 디렉터리 구분자로 /로 기재하였지만 사실 시스템별로 다르기때문에 file.separator를 사용하여 가져오는것이 좋다.

```

    // asset에 있는 파일을 쓰기 가능한 internal Storage로 복사한다.
    // Internal storage의 경로 구조는  /data/data/패키지명명

    final static String FILE_NAME= "파일이름";
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

```


* AsyncTask 파라메타 세개 정리 

```
    new AsyncTask<Void, Void, Void> {
        // Void의 의미
        // 첫번째 : do in background 메소드의 파라미터 타입. 입력은 execute에서 한다.
        // 두번째 : onProgressUpdate 의 파라미터
        // 세번째 : doInBackGround의 리턴타입
    }
```