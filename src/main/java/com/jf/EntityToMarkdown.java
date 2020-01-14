package com.jf;

import com.alibaba.fastjson.JSONObject;
import com.jf.bean.MyField;
import com.jf.config.RestTemplateConfig;
import com.jf.conventer.WxMappingJackson2HttpMessageConverter;
import com.jf.ssl.Myssl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author 江峰
 * @create 2020-01-10   13:08
 */
public class EntityToMarkdown {
    public static void main(String[] args) throws IOException {
        String dirName = judgeDirName(args);
        List<String> entityFiles = getEntityFiles(dirName);

        // 遍历目录
        for (String pathName : entityFiles) {

            // 读取文件中的内容到字符串中
            String sb = readFile(pathName);

            // 获取字段内容："({" 中"{"开始对应的"}"中间的内容
            String fieldsContent = getFieldsContent(sb);

            // 将字符串的内容中的各个字段提取出来
            List<String> list = getEvenFieldContent(fieldsContent);

            // 一个实体类中的所有字段对象
            List<MyField> myFields = new ArrayList<MyField>();
            for (String s : list) {
                myFields.add(transformToObj(s));
            }

            // 获取文件名称
            String fileName = getFileName(pathName);

            // 转换为markdown语法
            String content = transObjToMarkdown(myFields, fileName);

            // 上传到showdoc
            saveToShowdoc(content, fileName);
            System.out.println("上传结束");

            // 保存文件到本地
            saveContentToFile(content, fileName);
        }
    }

    static String judgeDirName(String[] args) {
        if (args.length == 0) {
            Assert.isTrue(false, "请在java -jar xxx.jar后面输入数据库xxxEntity.js所在的model目录路径");
        }
        String dirName = args[0];
        File file = new File(dirName);
        if (!file.exists()) {
            Assert.isTrue(false, "输入的路径有误，请重新输入");
        }
        return dirName;
    }

    /**
     * 上传到showdoc
     *
     * @param content
     * @param fileName
     */
    static void saveToShowdoc(String content, String fileName) {
        RestTemplate restTemplate = RestTemplateConfig.getRestTemplate();
        String url = "https://www.showdoc.cc/server/api/item/updateByApi";
        HttpEntity<Object> entity = buildRequest(content, fileName);
        Map map = restTemplate.postForObject(url, entity, HashMap.class);
        // if (map.get())
        // System.out.println(map);
    }

    /**
     * 构建请求数据（header，params）
     *
     * @return
     */
    static HttpEntity<Object> buildRequest(String content, String fileName) {
        JSONObject json = new JSONObject();
        json.put("api_key", "20e873f364c7beb840660b7dac0627ed861115895");
        json.put("api_token", "94e8f3c62a5023a36eb43226a626496e414055593");
        json.put("cat_name", "数据库");
        json.put("page_title", fileName + "表");
        json.put("page_content", content);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Object> entity = new HttpEntity<Object>(json, headers);
        return entity;
    }

    static void saveContentToFile(String content, String fileName) {
        //  文件保存路径
        String savefile = "C:\\Users\\Administrator\\Desktop\\model\\" + fileName + ".md";
        saveAsFileWriter(content, savefile);
    }

    /**
     * 将field对象转换为markdown。
     *
     * @param myFields
     */
    static String transObjToMarkdown(List<MyField> myFields, String fileName) {
        StringBuilder content = new StringBuilder();
        content.append("-  " + fileName + "表" + "\r\n" + "\r\n");
        content.append("|字段信息 | 类型 | 必填 | 默认 | 备注信息 |" + "\r\n");
        content.append("|:----    |:-------    |:--- |-- -|------      |" + "\r\n");
        String str = content.toString();
        str = addFieldContentToMarkdown(str, myFields);
        return str;
    }


    /**
     * 将每个字符串对象转换为对象。
     *
     * @param str
     * @return
     */
    static MyField transformToObj(String str) {
        MyField myField = new MyField();

        // 设置名称和注释
        str = setMyFieldNameAndComment(str, myField);
        str = str.replaceAll("\n", "");
        if (str.indexOf("{") >= 0) {
            transMyFieldContentToObjContainsBrackets(str, myField);
        } else {
            transMyFieldContentToObjNotContainsBrackets(str, myField);
        }
        return myField;
    }

    /**
     * 获取数据库model目录中的所有entity文件（全路径）,
     *
     * @param dirName
     * @return
     */
    static List<String> getEntityFiles(String dirName) {
        List<String> files = new ArrayList<String>();
        File file = new File(dirName);
        File[] tempList = file.listFiles();
        for (File f : tempList) {
            if (f.isFile() && f.getName().contains("Entity")) {
                files.add(f.toString());
            }
        }
        return files;
    }

    /**
     * 添加字段内容
     *
     * @param content
     * @param myFields
     * @return
     */
    static String addFieldContentToMarkdown(String content, List<MyField> myFields) {
        for (MyField myField : myFields) {
            content += "|" + myField.getName() + "|" + myField.getType() + "|" + myField.getRequired()
                    + "|" + myField.getDefaultValue() + "|" + myField.getComment() + "|\r\n";
        }
        return content;
    }

    public static void saveAsFileWriter(String content, String savefile) {
        FileWriter fwriter = null;
        try {
            File file = new File(savefile);
            if (!file.exists()) {
                file.createNewFile();
            }
            fwriter = new FileWriter(savefile);
            fwriter.write(content);
        } catch (
                IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取文件名称
     *
     * @param pathName
     * @return
     */
    static String getFileName(String pathName) {
        pathName = pathName.trim();
        String fileName = "";
        int index = pathName.indexOf("Entity");
        if (pathName.contains("/")) {
            fileName = pathName.substring(pathName.lastIndexOf("/") + 1, index);
        } else if (pathName.contains("\\")) {
            fileName = pathName.substring(pathName.lastIndexOf("\\") + 1, index);
        }
        return fileName;
    }

    /**
     * 将不包含{}的字段转换为对象
     *
     * @param str
     * @param myField
     * @return
     */
    static MyField transMyFieldContentToObjNotContainsBrackets(String str, MyField myField) {
        int index = str.indexOf(":");
        // 尾部有逗号
        int i = str.indexOf(",");
        String type = "";
        if (i >= 0) {
            type = str.substring(index + 1, i);
        } else {
            type = str.substring(index + 1);
        }
        myField.setType(type);
        return myField;
    }

    /**
     * 读取一个文件中的内容到字符串中来
     *
     * @param pathName
     * @return
     */
    static String readFile(String pathName) {
        try {
            FileReader reader = new FileReader(pathName);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据,读取的一行数据为空时,就跳过重读一行。
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                line = line.replaceAll(" ", "");
                sb.append(line + "\r\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字符串的内容中的各个字段提取出来
     *
     * @param fieldsContent
     * @return
     */
    static List<String> getEvenFieldContent(String fieldsContent) throws IOException {
        if (StringUtils.isEmpty(fieldsContent)) {
            return new ArrayList<String>();
        }

        // 将字符串读取到字符流中
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                fieldsContent.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line = "";
        StringBuilder tempString = new StringBuilder();
        boolean isMatch = false;
        List<String> list = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            if (line.startsWith("//")) {
                tempString.append(line + "\n");
                continue;
            }
            if (tempString.toString().startsWith("/*") && !(tempString.toString().endsWith("*/"))) {
                tempString.append(line + "\n");
                continue;
            }
            if (!isMatch) {
                String pattern = "[A-Za-z]*:\\{.*";
                String pattern1 = "[A-Za-z]*:\\[\\{.*";
                boolean a = Pattern.matches(pattern, line);
                boolean b = Pattern.matches(pattern1, line);
                if (a || b) {
                    isMatch = true;
                }
            }
            if (isMatch) {
                int i = line.indexOf("//");
                int i1 = line.indexOf("}");
                int i2 = line.indexOf("}]");

                if ((i > 0 && i < i1) || (i > 0 && i < i2)) {
                    tempString.append(line + "\n");
                    continue;
                } else if (i1 >= 0 || i2 >= 0) {
                    tempString.append(line);
                    list.add(tempString.toString());
                    tempString.replace(0, tempString.length(), "");
                    isMatch = false;
                    continue;
                } else {
                    tempString.append(line + "\n");
                    continue;
                }
            } else {
                tempString.append(line);
                list.add(tempString.toString());
                tempString.replace(0, tempString.length(), "");
                isMatch = false;
            }
        }
        return list;
    }

    static String removeSpaces(String str) {
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (c == ' ') {
                continue;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 获取字段内容："({" 中"{"开始对应的"}"中间的内容
     */
    static String getFieldsContent(String str) {
        int start = str.indexOf("({");
        if (start < 0) {
            return "";
        }
        int end = str.indexOf("})", start);
        if (end < 0) {
            return "";
        }
        str = str.substring(start + 1, end + 1);
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            String temp = str.substring(i, i + 1);
            if (temp.equals("{")) {
                count++;
            }
            if (temp.equals("}")) {
                count--;
            }
            if (count == 0) {
                str = str.substring(1, i);
            }
        }
        return str;
    }

    /**
     * 将包含{}的字段转换为对象
     *
     * @param str
     * @return
     */
    static void transMyFieldContentToObjContainsBrackets(String str, MyField myField) {
        int left = str.indexOf("{");
        int right = str.indexOf("}");
        String objs = str.substring(left + 1, right);
        String[] kv = objs.split(",");
        for (String s : kv) {
            String[] split = s.split(":");
            setMyField(myField, split);
        }
    }

    /**
     * 设置 类型，默认值 和 必填
     *
     * @param myField
     * @param arr
     */
    static void setMyField(MyField myField, String[] arr) {
        if (arr.length != 2) {
            return;
        }
        if (arr[0].contains("type")) {
            myField.setType(arr[1]);
        } else if (arr[0].contains("default")) {
            myField.setDefaultValue(arr[1]);
        } else if (arr[0].contains("required")) {
            myField.setRequired(new Boolean(arr[1]));
        }
    }

    /**
     * 获取字段的名字
     *
     * @param str
     * @return
     */
    static String setMyFieldNameAndComment(String str, MyField myField) {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        StringBuilder sb = new StringBuilder();
        StringBuilder comment = new StringBuilder();
        boolean flag = false;
        try {
            while ((line = br.readLine()) != null) {
                if (!flag) {
                    if (line.matches("[A-Za-z]*:.*")) {
                        int index = line.indexOf(":");
                        myField.setName(line.substring(0, index));
                        flag = true;
                    }
                }
                int index = line.indexOf("//");
                if (index >= 0) {
                    comment.append(line.substring(index + 2, line.length()));
                    line = line.substring(0, index);
                }
                if (line != "" && line.length() > 0) {
                    sb.append(line + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("comment = " + comment);
        myField.setComment(comment.length() == 0 ? null : comment.toString());
        // System.out.println("comment after = " + myField.getComment());
        return sb.toString();
    }
}
