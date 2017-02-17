package com.webcontrol.javaxxz;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jsoup.select.Elements;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.db.JDBCHelper;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

public class TutorialCrawler extends BreadthCrawler {

	static JdbcTemplate jdbcTemplate = null;
	
    public TutorialCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    /*
        可以往next中添加希望后续爬取的任务，任务可以是URL或者CrawlDatum
        爬虫不会重复爬取任务，从2.20版之后，爬虫根据CrawlDatum的key去重，而不是URL
        因此如果希望重复爬取某个URL，只要将CrawlDatum的key设置为一个历史中不存在的值即可
        例如增量爬取，可以使用 爬取时间+URL作为key。

        新版本中，可以直接通过 page.select(css选择器)方法来抽取网页中的信息，等价于
        page.getDoc().select(css选择器)方法，page.getDoc()获取到的是Jsoup中的
        Document对象，细节请参考Jsoup教程
    */
    @Override
    public void visit(Page page, CrawlDatums next) {
    	try{
    		  System.out.println("pageurl:"+page.getUrl());
    		  if (page.matchUrl("http://www.javaxxz.com/thread-\\d+\\-1-\\d+\\.html")) {
    			    Elements event =  page.select("meta[name=keywords]");
    			    String title = event.attr("content");
    			    if(title.contains("下载")){
    			    	  System.out.println("内容为下载:");
    			    }
    	            String content = page.select("div[class=t_fsz]").first().text();
    	            System.out.println("title:"+title);
    	            insertDb(title,content);
    	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    public static void main(String[] args) throws Exception {
    	initJdbc();
        TutorialCrawler crawler = new TutorialCrawler("crawler", true);
        for(int i=1;i<135;i++){
        	 String seed = "http://www.javaxxz.com/forum-140-"+i+".html";
        	 crawler.addSeed(seed);
        }
//        http://lib.csdn.net/base/java?page=21#md
//        http://www.javaxxz.com/thread-203208-6-2.html
        crawler.addRegex("http://www.javaxxz.com/thread-\\d+\\-1-\\d+\\.html");
        /*可以设置每个线程visit的间隔，这里是毫秒*/
        crawler.setExecuteInterval(2000);
        /*可以设置http请求重试的间隔，这里是毫秒*/
        crawler.setThreads(4);
        crawler.start(2);
    }
    
    
   private static void  initJdbc(){
	   try {
	        jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
	                "jdbc:mysql://localhost/webcontrol?useUnicode=true&characterEncoding=utf8",
	                "webcontrol", "HMnb15fXedysHCnE", 5, 30);
	        System.out.println("mysql连接成功");
	    } catch (Exception ex) {
	        jdbcTemplate = null;
	        System.out.println("mysql连接失败");
	    }
   }
   
   
   private static void insertDb(String title,String content){
	   try{
		   if (jdbcTemplate != null) {
	
			    jdbcTemplate.query("select max(pid) from webcontrolforum_post;",new ResultSetExtractor() {
					@Override
					public Object extractData(ResultSet arg0) throws SQLException, DataAccessException {
						if(null!=arg0){
							if(arg0.next()){
								 int count = arg0.getInt(1);
		                         String sql = getInsertContentSql(count+1,title,content);
		                         int update = jdbcTemplate.update(sql);
							}
						}
						// TODO Auto-generated method stub
						return null;
					}
		        });
			    jdbcTemplate.query("select max(tid) from webcontrolforum_thread;",new ResultSetExtractor() {
					@Override
					public Object extractData(ResultSet arg0) throws SQLException, DataAccessException {
						if(null!=arg0){
							if(arg0.next()){
								 int count = arg0.getInt(1);
		                         String sql = getInsertTitileSql(count+1,title);
	                             int update = jdbcTemplate.update(sql);
							}
						}
						// TODO Auto-generated method stub
						return null;
					}
		        });
			    jdbcTemplate.update("INSERT INTO `webcontrolcommon_member_field_home` VALUES ('1', '', '', '', '', '0', '0', '0', '', '', '', '"+title+"', '', '', '', '', '', '');");
			}
		   
	   }catch(Exception e){
		   e.printStackTrace();
	   }
   }
   
   public static String getInsertTitileSql(int count,String title){
	   try{
			 StringBuilder  content = new StringBuilder();
			 content.append("INSERT INTO `webcontrolforum_thread` VALUES ('");
			 content.append(count);
			 content.append( "','36', '0', '0', '0', '0', '0', 'admin', '1', '");
			 content.append(title);
			 content.append("',"+ "'");
			 content.append(System.currentTimeMillis()/1000);
			 content.append("',"+ " '");
			 content.append(System.currentTimeMillis()/1000);
			 content.append("', 'admin', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '32', '0', '0', '0', '-1', '-1', '0', '0', '0', '0', '0', '', '0', '0');");
		     System.out.println(content.toString());
			 return content.toString();
	   }catch (Exception e) {
		   e.printStackTrace();
		   return "";
	   }
   }
   
   
   
   public static String getInsertContentSql(int count,String title,String con){
	   try{
			 StringBuilder  content = new StringBuilder();
			 content.append("INSERT INTO `webcontrolforum_post` VALUES ('");
			 content.append(count);
			 content.append( "', '36', '");
			 content.append(count);
			 content.append("', '1', 'admin', '1', '");
			 content.append(title);
			 content.append("',"+ "'");
			 content.append(System.currentTimeMillis()/1000);
			 content.append("',"+ " '");
			 content.append(con);
			 content.append("', '::1', '61845', '0', '0', '1', '0', '-1', '-1', '0', '0', '0', '0', '0', '', '0', '0', '1');");
		     System.out.println(content.toString());
		   return content.toString();
	   }catch (Exception e) {
		   e.printStackTrace();
		   return "";
	   }
   }
}