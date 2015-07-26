package org.frameworkset.platform.genproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipException;

import org.frameworkset.runtime.CommonLauncher;
import org.frameworkset.util.io.ClassPathResource;

import bboss.org.apache.velocity.Template;
import bboss.org.apache.velocity.VelocityContext;

import com.frameworkset.util.FileUtil;
import com.frameworkset.util.VelocityUtil;

public class GenService {
	private String eclipseworkspace;
	private String projectname;
	private boolean initdb;
	private boolean clearproject;
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private String db_init_tool;
	private String war;
	private boolean inited;

	private File projectpath;

	private File projectwebrootpath;
	private File projectdbinitpath;
	private File projectresourcepath;
	private String validationQuery;

	public GenService() {
		// TODO Auto-generated constructor stub
	}

	public void init() {
		if (inited)
			return;
		inited = true;
		eclipseworkspace = CommonLauncher.getProperty("eclipseworkspace",
				"D:\\testwp");// elipse工程工作目录
		projectname = CommonLauncher.getProperty("projectname", "ptest");// 要生成的工程目录

		projectpath = new File(eclipseworkspace, projectname);
		projectdbinitpath = new File(projectpath, "dbinit-system");
		projectwebrootpath = new File(projectpath, "WebRoot");
		projectresourcepath = new File(projectpath, "resources");
		clearproject = Boolean.parseBoolean(CommonLauncher.getProperty(
				"clearproject", "true"));
		if (clearproject && projectpath.exists())
			projectpath.delete();
		if (!projectpath.exists())
			projectpath.mkdirs();
		if (!projectdbinitpath.exists())
			projectdbinitpath.mkdirs();
		if (!projectwebrootpath.exists())
			projectwebrootpath.mkdirs();
		if (!projectresourcepath.exists())
			projectresourcepath.mkdirs();
		initdb = Boolean.parseBoolean(CommonLauncher.getProperty("initdb",
				"true"));

		driverClassName = CommonLauncher.getProperty("driverClassName",
				"oracle.jdbc.driver.OracleDriver");// 要生成的工程目录
		url = CommonLauncher.getProperty("url",
				"jdbc:oracle:thin:@//localhost:1521/orcl");// 要生成的工程目录
		username = CommonLauncher.getProperty("username", "BBOSSTEST");// 要生成的工程目录
		password = CommonLauncher.getProperty("password", "BBOSSTEST");// 要生成的工程目录
		validationQuery = CommonLauncher.getProperty("validationQuery",
				"select 1 from dual");// 要生成的工程目录
		db_init_tool = CommonLauncher.getProperty("db_init_tool",
				"D:\\d\\workspace\\bboss-cms\\distrib\\dbinit-system.zip");// 要生成的工程目录
		war = CommonLauncher.getProperty("war",
				"D:\\d\\workspace\\bboss-cms\\distrib\\WebRoot.war");// 要生成的工程目录

	}

	public void gen() {
		init();
		try {
			unzipArchs();
			copyresources();
			genProject();
			initDB();
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void copyresources() throws IOException {
		FileUtil.copy(new File(projectwebrootpath, "WEB-INF/classes"),
				this.projectresourcepath.getAbsolutePath());
	}

	private void unzipArchs() throws ZipException, IOException {
		FileUtil.unzip(war, projectwebrootpath.getAbsolutePath());
		FileUtil.unzip(this.db_init_tool,
				this.projectdbinitpath.getAbsolutePath());
	}
	private void setupdbinitpath(String startuppath,String startupfilename)
	{
		FileWriter writer = null;
		try {
			// 生成ant构建属性文件
			Template antbuild = VelocityUtil.getTemplate(startuppath);
			VelocityContext context = new VelocityContext();// VelocityUtil.buildVelocityContext(context)
			context.put("dbinitpath", "cd "+this.projectdbinitpath.getCanonicalFile());
			writer = new FileWriter(new File(projectdbinitpath, startupfilename));
			antbuild.merge(context, writer);
			writer.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	private void recoverdbinitpath(String startuppath,String startupfilename)
	{
		FileWriter writer = null;
		try {
			// 生成ant构建属性文件
			Template antbuild = VelocityUtil.getTemplate(startuppath);
			VelocityContext context = new VelocityContext();// VelocityUtil.buildVelocityContext(context)
			context.put("dbinitpath", "");
			writer = new FileWriter(new File(projectdbinitpath, startupfilename));
			antbuild.merge(context, writer);
			writer.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	private void initDB() throws IOException {
		if (this.initdb)// 如果需要用执行数据库初始化，则执行以下数据库初始化功能
		{

			try {
				Process proc = null;
				if (CommonLauncher.isLinux()) {
//					${dbinitpath}
					setupdbinitpath("dbinit/startup.sh","startup.sh");
					proc = Runtime.getRuntime().exec(
							new File(this.projectdbinitpath, "/startup.sh")
									.getCanonicalPath());
				} else
				{
					setupdbinitpath("dbinit/startup.bat","startup.bat");
					proc = Runtime.getRuntime().exec(
							new File(this.projectdbinitpath, "/startup.bat")
									.getCanonicalPath());
				}
				StreamGobbler error = new StreamGobbler( proc.getErrorStream(),"ERROR");
				
				StreamGobbler normal = new StreamGobbler( proc.getInputStream(),"NORMAL");
				error.start();
				normal.start();
//				InputStream stderr = proc.getErrorStream();
//				InputStreamReader isr = new InputStreamReader(stderr);
//				BufferedReader br = new BufferedReader(isr);
//				String line = null;
//				System.out.println(" ");
//				while ((line = br.readLine()) != null)
//					System.out.println(line);
//				System.out.println("");
				int exitVal = proc.waitFor();
				System.out.println("初始化数据库完毕! " );
			} catch (Throwable t) {
				t.printStackTrace();
			}
			finally
			{
				if (CommonLauncher.isLinux()) {
//					${dbinitpath}
					recoverdbinitpath("dbinit/startup.sh","startup.sh");
				
				} else
				{
					recoverdbinitpath("dbinit/startup.bat","startup.bat");
					
				}
			}
		}
	}

	private void genjavaprojectfile() {

		FileWriter writer = null;
		try {
			// 生成ant构建属性文件
			Template antbuild = VelocityUtil.getTemplate("project/.project");
			VelocityContext context = new VelocityContext();// VelocityUtil.buildVelocityContext(context)
			context.put("project", this.projectname);
			writer = new FileWriter(new File(this.projectpath, ".project"));
			antbuild.merge(context, writer);
			writer.flush();
			ClassPathResource resource = new ClassPathResource(
					"templates/project/.classpath");
			resource.savetofile(new File(this.projectpath, ".classpath"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private void gendbpoolfile() {

		FileWriter writer = null;
		try {
			// 生成ant构建属性文件
			Template antbuild = VelocityUtil.getTemplate("resources/dbcp.xml");
			VelocityContext context = new VelocityContext();// VelocityUtil.buildVelocityContext(context)

			context.put("driverClassName", this.driverClassName);
			context.put("url", this.url);
			context.put("username", this.username);
			context.put("password", this.password);
			context.put("validationQuery", this.validationQuery);
			writer = new FileWriter(new File(this.projectresourcepath,
					"dbcp.xml"));
			antbuild.merge(context, writer);
			writer.flush();
			ClassPathResource resource = new ClassPathResource(
					"templates/resources/poolman.xml");
			resource.savetofile(new File(this.projectresourcepath,
					"poolman.xml"));
			FileUtil.copy(new File(this.projectresourcepath, "dbcp.xml"),
					new File(this.projectdbinitpath, "resources")
							.getAbsolutePath());
			FileUtil.copy(new File(this.projectresourcepath, "poolman.xml"),
					new File(this.projectdbinitpath, "resources")
							.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private void genantbuildfile() {
		FileWriter writer = null;
		try {
			// 生成ant构建属性文件
			Template antbuild = VelocityUtil
					.getTemplate("ant/build.properties");
			VelocityContext context = new VelocityContext();// VelocityUtil.buildVelocityContext(context)
			context.put("projectname", this.projectname);
			writer = new FileWriter(new File(this.projectpath,
					"build.properties"));
			antbuild.merge(context, writer);
			writer.flush();
			ClassPathResource resource = new ClassPathResource(
					"templates/ant/build.bat");
			resource.savetofile(new File(this.projectpath, "build.bat"));
			resource = new ClassPathResource("templates/ant/build.xml");
			resource.savetofile(new File(this.projectpath, "build.xml"));
			resource = new ClassPathResource("templates/ant/buildjar.bat");
			resource.savetofile(new File(this.projectpath, "buildjar.bat"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private void genProject() throws IOException {
		genantbuildfile();
		genjavaprojectfile();
		gendbpoolfile();

	}
}