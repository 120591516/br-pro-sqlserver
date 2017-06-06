package jphs.log.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jphs.log.mapper.AccesslogMapper;
import jphs.log.mapper.ProductMapper;
import jphs.log.pojo.Accesslog;
import jphs.log.pojo.AccesslogSpread;
import jphs.log.pojo.Product;
import jphs.log.pojo.ProductExample;
import jphs.log.pojo.ProductExample.Criteria;
import jphs.log.utils.MyPredicate;

@Service
public class ParseLogs {
	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private AccesslogMapper accesslogMapper;

	private static String baseUrlPrefix = "/ComeIn?m=setOneProductNew&";// 微信服务号平台

	private static String wxNurse114UrlPrefix = "/wxNurse114";// 微信114生活助手
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
	 * 读取文件信息 微信公众号日志中存在114微信助手访问信息
	 * 
	 * @param fileName
	 *            文件路径D:\Program
	 *            Files\eclipse\workspace\ParseLogTest\src\access_20170604.log
	 * @throws Exception
	 */
	public void readFileByLines() {
		// 先获取未解析日志时间
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date time = cal.getTime();
		String yesterday = dayFormat.format(time);
		// 文件路径
		// String filePath = "";
		String fileName = "D:/Program Files/eclipse/workspace/br-pro-sqlserver/src/main/java/access_20170604.log";
		// 定义最终解析完纯净数据的集合
		List<Accesslog> logList = new ArrayList<>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			// 以行为单位读取文件内容，一次读一整行
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
			reader = new BufferedReader(isr);
			String tempString = null;
			// 筛选出的数据
			List<AccesslogSpread> wxList = new ArrayList<AccesslogSpread>();
			List<AccesslogSpread> wxNurse114List = new ArrayList<AccesslogSpread>();
			AccesslogSpread al = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 获取访问时间的小时数
				// 获取当前访问时间的时分秒
				if (tempString.contains(baseUrlPrefix)) {
					al = new AccesslogSpread();
					int timeIndex = tempString.indexOf(":");
					String hourse = tempString.substring(timeIndex + 1, timeIndex + 3);
					String startTime = hourse + ":00:00";
					String endTime = hourse + ":59:59";
					// 获取ip地址 根据ip判断pv、uv
					int ipindex = tempString.indexOf("-");
					String ipaddress = tempString.substring(0, ipindex - 1);
					// 获取产品地址
					int urlindex = tempString.indexOf(baseUrlPrefix);
					String urladdress = tempString.substring(urlindex, urlindex + baseUrlPrefix.length() + 5);
					al.setAccesstime(dayFormat.parse(yesterday));
					al.setIp(ipaddress);
					al.setStarttime(timeFormat.parse(startTime));
					al.setEndtime(timeFormat.parse(endTime));
					al.setProductPath(urladdress);
					if (tempString.contains(wxNurse114UrlPrefix)) {
						al.setPlatformId(2);
						wxNurse114List.add(al);
					} else {
						al.setPlatformId(1);
						wxList.add(al);

					}
				}
			}
			reader.close();
			extracted(logList, wxList, yesterday);
			extracted(logList, wxNurse114List, yesterday);
		} catch (Exception e) {
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

	private void extracted(List<Accesslog> logList, List<AccesslogSpread> list, String dayTime)
			throws ParseException, IllegalAccessException, InvocationTargetException {
		List<AccesslogSpread> allProduct = new ArrayList<>(list);
		// 获取所有访问商品列表
		for (int i = 0; i < allProduct.size(); i++) {
			for (int j = allProduct.size() - 1; j > i; j--) {
				if (allProduct.get(i).getProductPath().equals(allProduct.get(j).getProductPath())) {
					allProduct.remove(j);
				}
			}
		}
		Accesslog accesslog = null;
		Accesslog sourceTime = null;
		for (AccesslogSpread accesslog2 : allProduct) {
			// 根据商品的地址获取商品一天内的访问次数
			Predicate predicate = new MyPredicate("productPath", accesslog2.getProductPath());
			List<AccesslogSpread> select = (List<AccesslogSpread>) CollectionUtils.select(list, predicate);
			ProductExample productExample = new ProductExample();
			Criteria productCriteria = productExample.createCriteria();
			productCriteria.andPathEqualTo(accesslog2.getProductPath());
			List<Product> product = productMapper.selectByExample(productExample);
			if (product.size() > 0) {
				System.out.println(product.get(0).getId());

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
					sourceTime = new Accesslog();
					BeanUtils.copyProperties(sourceTime, accesslog3);
					sourceTime.setProductId(product.get(0).getId());
					Predicate pvPredicate = new MyPredicate("starttime", accesslog3.getStarttime());
					List<AccesslogSpread> pv = (List<AccesslogSpread>) CollectionUtils.select(select, pvPredicate);
					sourceTime.setPv(pv.size());
					// 如果时间段的日志数==1说明该时间段有且只有一个ip访问pv、uv值相等
					// 反之就进行判断
					if (pv.size() > 1) {
						for (int i = 0; i < pv.size() - 1; i++) {
							for (int j = 1; j < pv.size(); j++) {
								if (pv.get(i).getIp().equals(pv.get(j).getIp())) {
									pv.remove(j);
								}
							}
						}
					}
					sourceTime.setUv(pv.size());
					logList.add(sourceTime);
				}
				accesslog = new Accesslog();
				BeanUtils.copyProperties(accesslog, accesslog2);
				accesslog.setProductId(product.get(0).getId());
				accesslog.setStarttime(timeFormat.parse("00:00:00"));
				accesslog.setEndtime(timeFormat.parse("23:59:59"));
				accesslog.setPv(select.size());
				for (int i = 0; i < select.size(); i++) {
					for (int j = select.size() - 1; j > i; j--) {
						if (select.get(i).getIp().equals(select.get(j).getIp())) {
							select.remove(j);
						}
					}
				}
				accesslog.setUv(select.size());
				logList.add(accesslog);
			}
		}
		for (Accesslog accesslog1 : logList) {
			// 将数据插入到数据库
			accesslogMapper.insertSelective(accesslog1);
		}
	}
}
