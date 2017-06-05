package jphs.log.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import jphs.log.pojo.Accesslog;
import jphs.log.pojo.AccesslogSpread;
import jphs.log.utils.MyPredicate;

public class ParseLog {
	private static String wxUrlPrefix = "/ComeIn?m=setOneProductNew&";// 微信服务号平台
	private static String wxNurse114UrlPrefix = "/wxNurse114/ComeIn?m=setOneProductNew&";// 微信114生活助手
	// private static String APPLYSTRING = "写过的新闻";
	// private static String SEARCHSTRING = "正在搜索新闻：";
	// private static String USERTAG = "用户：";
	/**
	 * 用戶名，用戶行為，以key-value方式存储 用户行为 以json方式存储
	 * 
	 * @throws Exception
	 */
	private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * 读取文件信息
	 * 
	 * @param fileName
	 *            文件路径D:\Program
	 *            Files\eclipse\workspace\ParseLogTest\src\access_20170604.log
	 * @throws Exception
	 */
	public static void readFileByLines(String fileName) throws Exception {
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
			reader = new BufferedReader(isr);
			String tempString = null;
			List<AccesslogSpread> list = new ArrayList<AccesslogSpread>();
			AccesslogSpread accesslog = null;
			String dayTime = fileName.substring(fileName.length() - 12, fileName.length() - 4);
			System.out.println("当前时间：" + dayTime);
			System.out.println(dayFormat.parse(dayTime));
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 获取访问时间的小时数
				// 获取当前访问时间的时分秒
				if (tempString.contains(wxUrlPrefix)) {
					accesslog = new AccesslogSpread();
					int timeIndex = tempString.indexOf(":");
					String hourse = tempString.substring(timeIndex + 1, timeIndex + 3);
					System.out.println("开始的小时" + hourse);
					String startTime = hourse + ":00:00";
					String endTime = hourse + ":59:59";
					System.out.println("开始时间" + startTime);
					System.out.println(timeFormat.parse(startTime));
					System.out.println("结束时间" + endTime);
					System.out.println(timeFormat.parse(endTime));
					// 获取ip地址
					int ipindex = tempString.indexOf("-");
					String ipaddress = tempString.substring(0, ipindex - 1);
					System.out.println(ipaddress);
					// 获取产品地址
					int urlindex = tempString.indexOf(wxUrlPrefix);
					String urladdress = tempString.substring(urlindex, urlindex + wxUrlPrefix.length() + 5);
					System.out.println(urladdress);
					// 获取访问时间
					int dateStartIndex = tempString.indexOf("[");
					int dateEndIndex = tempString.indexOf("]");
					String dateStr = tempString.substring(dateStartIndex + 1, dateEndIndex);
					System.out.println(dateStr);
					accesslog.setAccesstime(dayFormat.parse(dayTime));
					accesslog.setIp(ipaddress);
					accesslog.setStarttime(timeFormat.parse(startTime));
					accesslog.setEndtime(timeFormat.parse(endTime));
					accesslog.setProductPath(urladdress);
					list.add(accesslog);
				}
			}
			reader.close();
			for (Accesslog accesslog2 : list) {

				System.out.println(accesslog2);
			}
			List<AccesslogSpread> allProduct = new ArrayList<>(list);
			// 获取所有访问商品列表
			for (int i = 0; i < allProduct.size(); i++) {
				for (int j = allProduct.size() - 1; j > i; j--) {
					if (allProduct.get(i).getProductPath().equals(allProduct.get(j).getProductPath())) {
						allProduct.remove(j);
					}
				}
			}
			for (AccesslogSpread accesslog2 : allProduct) {
				// 根据商品的地址获取商品一天内的访问次数
				System.out.println("访问地址是" + accesslog2.getProductPath());
				Predicate predicate = new MyPredicate("productPath", accesslog2.getProductPath());
				List<AccesslogSpread> select = (List<AccesslogSpread>) CollectionUtils.select(list, predicate);
				// 获取某一商品的各个时间段
				List<AccesslogSpread> timeList = new ArrayList<>(select);
				for (int i = 0; i < timeList.size(); i++) {
					for (int j = timeList.size() - 1; j > i; j--) {
						if (timeList.get(i).getStarttime().equals(timeList.get(j).getStarttime())) {
							timeList.remove(j);
						}
					}
				}
				for (AccesslogSpread accesslog3 : timeList) {
					Predicate pvPredicate = new MyPredicate("starttime", accesslog3.getStarttime());
					List<AccesslogSpread> pv = (List<AccesslogSpread>) CollectionUtils.select(select, pvPredicate);
					System.out.println("时间段" + timeFormat.format(accesslog3.getStarttime()) + "--"
							+ timeFormat.format(accesslog3.getEndtime()) + "的pv次数" + pv.size());
					if (pv.size() > 1) {
						for (int i = 0; i < pv.size() - 1; i++) {
							for (int j = 1; j < pv.size(); j++) {
								if (pv.get(i).getIp().equals(pv.get(j).getIp())) {
									pv.remove(j);
								}
							}
						}
					}
					System.out.println("时间段" + timeFormat.format(accesslog3.getStarttime()) + "--"
							+ timeFormat.format(accesslog3.getEndtime()) + "的uv次数" + pv.size());

				}

				System.out.println(accesslog2.getProductPath() + "------pv" + select.size());
				for (int i = 0; i < select.size(); i++) {
					for (int j = select.size() - 1; j > i; j--) {
						if (select.get(i).getIp().equals(select.get(j).getIp())) {
							select.remove(j);
						}
					}
				}
				System.out.println(accesslog2.getProductPath() + "------uv" + select.size());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Calendar cal = Calendar.getInstance();
		// System.out.println(Calendar.DATE);//5
		cal.add(Calendar.DATE, -1);
		Date time = cal.getTime();
		String yesterday = dayFormat.format(time);
		System.out.println(System.getProperty("user.dir"));
		System.out.println("\\src\\access_" + yesterday + ".log");
		// ParseLog.readFileByLines(System.getProperty("user.dir") +
		// "\\src\\access_" + yesterday + ".log");
		ParseLog.readFileByLines(System.getProperty("user.dir") + "\\src\\access_20170601.log");
	}

	/**
	 * 判断某一时间是否在一个区间内
	 * 
	 * @param sourceTime
	 *            时间区间,半闭合,如[10:00:00-20:00:00)
	 * @param curTime
	 *            需要判断的时间 如10:00:00
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static boolean isInTime(String sourceTime, String curTime) {
		if (sourceTime == null || !sourceTime.contains("-") || !sourceTime.contains(":")) {
			throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
		}
		if (curTime == null || !curTime.contains(":")) {
			throw new IllegalArgumentException("Illegal Argument arg:" + curTime);
		}
		String[] args = sourceTime.split("-");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try {
			long now = sdf.parse(curTime).getTime();
			long start = sdf.parse(args[0]).getTime();
			long end = sdf.parse(args[1]).getTime();
			if (args[1].equals("00:00:00")) {
				args[1] = "24:00:00";
			}
			if (end < start) {
				if (now >= end && now < start) {
					return false;
				} else {
					return true;
				}
			} else {
				if (now >= start && now < end) {
					return true;
				} else {
					return false;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
		}

	}
}
