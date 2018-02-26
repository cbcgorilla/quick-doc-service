package cn.mxleader.quickdoc.web.context;

import javax.servlet.http.HttpSession;

import cn.mxleader.quickdoc.web.domain.UploadProgress;
import org.apache.commons.fileupload.ProgressListener;
import org.springframework.stereotype.Component;

@Component
public class FileUploadProgressListener implements ProgressListener {
    private HttpSession session;
    public void setSession(HttpSession session){
        this.session=session;
        UploadProgress status = new UploadProgress();//保存上传状态
        session.setAttribute("status", status);
    }
    @Override
    public void update(long bytesRead, long contentLength, int items) {
        UploadProgress status = (UploadProgress) session.getAttribute("status");
        status.setBytesRead(bytesRead);
        status.setContentLength(contentLength);
        status.setItems(items);

    }

}