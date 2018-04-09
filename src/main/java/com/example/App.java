package com.example;

import com.alibaba.fastjson.JSON;
import com.opencsv.CSVWriter;
import com.taobao.pac.sdk.cp.PacClient;
import com.taobao.pac.sdk.cp.SendSysParams;
import com.taobao.pac.sdk.cp.dataobject.request.LD_GUESSCP.LdGuesscpRequest;
import com.taobao.pac.sdk.cp.dataobject.response.LD_GUESSCP.LdGuesscpResponse;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws IOException, InterruptedException {
        final PacClient pacClient = new PacClient("", "", "http://link.cainiao.com/gateway/link.do");

        File file = new File("1.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));

        Writer writer = new FileWriter(new File("1-output.csv"));
        CSVWriter csvWriter = new CSVWriter(writer, '|');

        ExecutorService executorService = Executors.newFixedThreadPool(100);

        String code = "";
        while((code = reader.readLine()) != null) {
            executorService.submit(new Task(code, pacClient, csvWriter));

            System.out.println(code);

//            Thread.sleep(1);
        }

//        Thread.sleep(5000);

//        csvWriter.flush();
//        csvWriter.close();


    }

    public static class Task implements Runnable {

        private String code;
        private PacClient pacClient;
        private CSVWriter csvWriter;

        public Task(String code, PacClient pacClient, CSVWriter csvWriter) {
            this.code = code;
            this.pacClient = pacClient;
            this.csvWriter = csvWriter;
        }

        public void run() {
            LdGuesscpRequest ldGuesscpRequest = new LdGuesscpRequest();
            ldGuesscpRequest.setAppName("dianwoda");
            ldGuesscpRequest.setMailNo(code);


            SendSysParams sendSysParams = new SendSysParams();
            sendSysParams.setToCode("LD-PACKPUSH"); //可以不填写，编码值向业务方确认
            sendSysParams.setFromCode("56984e556625d71e9c920c5383ad1922"); //一定要填写自己的cpcode , ISV请求填写token值


            String codeList = "";
            LdGuesscpResponse response = pacClient.send(ldGuesscpRequest,sendSysParams);

            if (response.isSuccess()) {
                codeList = JSON.toJSONString(response.getCpCodeList());
            } else {
                codeList = response.toString();
            }

            String[] strs = {code , codeList};
            csvWriter.writeNext(strs);
            try {
                csvWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(response.toString());
        }
    }

}
