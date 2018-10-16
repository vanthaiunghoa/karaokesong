package kr.ds.handler;

public class AppHandler {
	private String result;
	private String msg;
	private boolean ad;
	private boolean dev;
	private String admob_ad;
	private String admob_native;
	private boolean install;
	private String install_message;
	private String install_url;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isAd() {
		return ad;
	}

	public void setAd(boolean ad) {
		this.ad = ad;
	}

	public boolean isDev() {
		return dev;
	}

	public void setDev(boolean dev) {
		this.dev = dev;
	}

	public String getAdmob_ad() {
		return admob_ad;
	}

	public void setAdmob_ad(String admob_ad) {
		this.admob_ad = admob_ad;
	}

	public String getAdmob_native() {
		return admob_native;
	}

	public void setAdmob_native(String admob_native) {
		this.admob_native = admob_native;
	}

	public boolean isInstall() {
		return install;
	}

	public void setInstall(boolean install) {
		this.install = install;
	}

	public String getInstall_message() {
		return install_message;
	}

	public void setInstall_message(String install_message) {
		this.install_message = install_message;
	}

	public String getInstall_url() {
		return install_url;
	}

	public void setInstall_url(String install_url) {
		this.install_url = install_url;
	}
}
