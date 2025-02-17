package decode;

// import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class ReadTxt {

	public static void main(String[] args) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:decrypted_database.db");

			//输入SQL语句
			//String sql = "select * from message where msgId = 6";
			String sql;
			if (args.length == 0) {
				sql = "select * from message";
			}else{
				// SELECT * FROM table_name WHERE talker IN (n1, n2, n3);
				StringBuilder sb = new StringBuilder("SELECT * FROM message WHERE talker IN (");
				for (int i = 0; i < args.length; i++) {
					sb.append("'" + args[i] + "'");
					if (i < args.length - 1) {
						sb.append(",");
					}
				}
				sb.append(")");
				sql = sb.toString();
			}

			System.out.println(sql);

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(sql);
			String filePath = "./";
			List<Message> list = new ArrayList<Message>();
			while(rs.next()){
				//获取数据
				Message message = new Message();
				String msgId = rs.getString("msgId");
	            String content = rs.getString("content");
	            String reserved = rs.getString("reserved");
	            String lvbuffer = rs.getString("lvbuffer");
	            message.setMsgId(msgId);
	            message.setBizChatId(rs.getString("bizChatId"));
	            message.setBizChatUserId(rs.getString("bizChatUserId"));
	            message.setBizClientMsgId(rs.getString("bizClientMsgId"));
	            message.setCreateTime(rs.getString("createTime"));
	            message.setFlag(rs.getString("flag"));
	            message.setImgPath(rs.getString("imgPath"));
	            message.setIsSend(rs.getString("isSend"));
	            message.setIsShowTimer(rs.getString("isShowTimer"));
	            message.setMsgSeq(rs.getString("msgSeq"));
	            message.setMsgSvrId(rs.getString("msgSvrId"));
	            message.setStatus(rs.getString("status"));
	            message.setTalker(rs.getString("talker"));
	            message.setTalkerId(rs.getString("talkerId"));
	            message.setTransBrandWording(rs.getString("transBrandWording"));
	            message.setType(rs.getString("type"));

	          //  File file = null;

	            if(content != null && content.contains("~SEMI_XML~")){
//	            	file = new File(filePath+"\\SEMI_XML\\"+msgId+"-content");
//	            	message.setContent("$~SEMI_XML~content$");
//	            	ReadTxt.writeDocument(content, file);
	            	message.setContent(ReadTxt.ks(content));
	            	message.getHasXml().add("content");
	            }else{
	            	message.setContent(content);
	            }
	            if(reserved != null && reserved.contains("~SEMI_XML~")){
//	            	message.setReserved("$~SEMI_XML~reserved$");
//	            	file = new File(filePath+"\\SEMI_XML\\"+msgId+"-reserved");
//	            	ReadTxt.writeDocument(reserved, file);
	            	message.setReserved(ReadTxt.ks(reserved));
	            	message.getHasXml().add("reserved");
	            }else{
	            	message.setReserved(reserved);
	            }
//	            LvlbuffBean bean = new LvlbuffBean();
//	            ReadTxt.pI(lvbuffer.getBytes(), bean);
//	            message.setLvbuffer(bean);

	            if(lvbuffer != null && !"".equals(lvbuffer.trim())){
	            	ReadTxt.writeDocument(lvbuffer, new File(filePath+"\\BLOB\\"+msgId+"-lvbuffer"));
	            	message.setLvbuffer("$~BLOB~lvbuffer$");
	            }else{
	            	message.setLvbuffer("");
	            }
	            list.add(message);

			}
			String ss = JSON.toJSONString(list, SerializerFeature.WriteMapNullValue,SerializerFeature.PrettyFormat);
            ReadTxt.writeDocument(ss,new File(filePath+"\\"+"message"+".json"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeDocument(String by,File file){
		FileOutputStream fos = null;
		BufferedWriter osw =null;

        //判断目标文件所在的目录是否存在
        if(!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            // System.out.println("目标文件所在目录不存在，准备创建它！");
        	file.getParentFile().mkdirs();
        }

        try {
			fos = new FileOutputStream(file);
			osw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            osw.write(by);
            osw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(osw!=null){
                try {
                    osw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Map<String, String> ks(String str) {
        if (str == null || !str.startsWith("~SEMI_XML~")) {
            return null;
        }
        String substring = str.substring(10);
        Map<String, String> hashMap = new HashMap<>();
        int length = substring.length() - 4;
        int i = 0;
        while (i < length) {
            int i2 = i + 1;
            try {
                int i3 = i2 + 1;
                i = ((substring.charAt(i) << 16) + substring.charAt(i2)) + i3;
                String substring2 = substring.substring(i3, i);
                i3 = i + 1;
                int i4 = i3 + 1;
                i = ((substring.charAt(i) << 16) + substring.charAt(i3)) + i4;
                hashMap.put(substring2, substring.substring(i4, i));
            } catch (Throwable e) {
                e.printStackTrace();
                return hashMap;
            }
        }
        return hashMap;
    }

	public static void pI(byte[] field_lvbuff,LvlbuffBean bean) {

		try {

			if (field_lvbuff != null && field_lvbuff.length != 0) {

				WechatByteBufferUtil wechatByteBufferUtilVar = new WechatByteBufferUtil();

				int be = wechatByteBufferUtilVar.be(field_lvbuff);

				if (be != 0) {
					bean = null;
					System.out.print("---------------------------errro");
					return;
				}

				bean.setbAw(wechatByteBufferUtilVar.getInt());

				bean.setbAx(wechatByteBufferUtilVar.getInt());

				bean.setbAy(wechatByteBufferUtilVar.getString());

				bean.setbAz(wechatByteBufferUtilVar.getLong());

				bean.setUin(wechatByteBufferUtilVar.getInt());

				bean.setbAA(wechatByteBufferUtilVar.getString());

				bean.setBhc(wechatByteBufferUtilVar.getString());

				bean.setbAB(wechatByteBufferUtilVar.getInt());

				bean.setbAC(wechatByteBufferUtilVar.getInt());

				bean.setbAD(wechatByteBufferUtilVar.getString());

				bean.setbAE(wechatByteBufferUtilVar.getString());

				bean.setbAF(wechatByteBufferUtilVar.getInt());

				bean.setbAG(wechatByteBufferUtilVar.getInt());

				bean.setbAH(wechatByteBufferUtilVar.getString());

				bean.setbAI(wechatByteBufferUtilVar.getString());

				bean.setbAJ(wechatByteBufferUtilVar.getString());

				bean.setbAK(wechatByteBufferUtilVar.getString());

				bean.setbAL(wechatByteBufferUtilVar.getInt());

				bean.setBbt(wechatByteBufferUtilVar.getInt());

				bean.setbAM(wechatByteBufferUtilVar.getString());

				bean.setField_verifyFlag(wechatByteBufferUtilVar.getInt());

				bean.setbAN(wechatByteBufferUtilVar.getString());

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAO(wechatByteBufferUtilVar.getString());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAP(wechatByteBufferUtilVar.getInt());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAQ(wechatByteBufferUtilVar.getInt());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAR(wechatByteBufferUtilVar.getString());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAS(wechatByteBufferUtilVar.getString());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAT(wechatByteBufferUtilVar.getString());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAU(wechatByteBufferUtilVar.getString());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAV(wechatByteBufferUtilVar.getString());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAW(wechatByteBufferUtilVar.getString());

				}

				if (!wechatByteBufferUtilVar.bmL()) {

					bean.setbAX(wechatByteBufferUtilVar.getString());

				}

			}else{
				bean = null;
			}

		} catch (Exception e) {
			bean = null;
			e.printStackTrace();
			System.out.println("get value failed");

		}

	}


}
