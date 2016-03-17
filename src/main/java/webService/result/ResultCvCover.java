package webService.result;

import java.util.HashMap;
import java.util.List;

public class ResultCvCover {

	public ResultCvCover(ResultCvOrCover cv, ResultCvOrCover cover) {
		super();
		this.cv = cv;
		this.cover = cover;
	}
	public ResultCvOrCover getCv() {
		return cv;
	}
	public void setCv(ResultCvOrCover cv) {
		this.cv = cv;
	}
	public ResultCvOrCover getCover() {
		return cover;
	}
	public void setCover(ResultCvOrCover cover) {
		this.cover = cover;
	}
	public ResultCvOrCover cv;
	public ResultCvOrCover cover;
	
}