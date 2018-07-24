package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.common.utils.FileUtils;
import cn.mxleader.quickdoc.dao.SysDiskRepository;
import cn.mxleader.quickdoc.dao.SysFolderRepository;
import cn.mxleader.quickdoc.dao.ext.GridFsAssistant;
import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.web.domain.WebFile;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileServiceImpl implements FileService {

    private static final int BUFFER_SIZE = 8192;

    private final GridFsAssistant gridFsAssistant;
    private final SysDiskRepository sysDiskRepository;
    private final SysFolderRepository sysFolderRepository;
    private final MongoConverter converter;

    FileServiceImpl(GridFsAssistant gridFsAssistant,
                    SysDiskRepository sysDiskRepository,
                    SysFolderRepository sysFolderRepository,
                    MongoConverter converter) {
        this.gridFsAssistant = gridFsAssistant;
        this.sysDiskRepository = sysDiskRepository;
        this.sysFolderRepository = sysFolderRepository;
        this.converter = converter;
    }

    public WebFile getStoredFile(ObjectId fileId) {
        return switchWebFile(gridFsAssistant.findOne(new Query(Criteria.where("_id").is(fileId))));
    }

    /**
     * 在指定目录内查找相应名字的文件
     *
     * @param filename 输入文件名
     * @param parent   容器信息（容器ID， 容器类型：磁盘，文件夹）
     * @return
     */
    public WebFile getStoredFile(String filename, ParentLink parent) {
        Query query = Query.query(GridFsCriteria.whereFilename().is(filename));
        query.addCriteria(GridFsCriteria.whereMetaData("parents").in(parent));
        return switchWebFile(gridFsAssistant.findOne(query));
    }

    /**
     * 枚举目录内的所有文件
     *
     * @param folderId 所在目录ID
     * @return
     */
    public Stream<WebFile> getWebFiles(ObjectId folderId) {
        return StreamSupport.stream(switchWebFiles(getStoredFiles(folderId)).spliterator(), false);
    }

    private GridFSFindIterable getStoredFiles(ObjectId folderId) {
        Query query = Query.query(GridFsCriteria.whereMetaData("folderId").is(folderId));
        return gridFsAssistant.find(query);
    }

    @Override
    public List<WebFile> list(ParentLink parent) {
        Query query = Query.query(GridFsCriteria.whereMetaData("parents").in(parent));
        GridFSFindIterable fsFiles = gridFsAssistant.find(query);
        return StreamSupport.stream(switchWebFiles(fsFiles).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public Page<WebFile> list(ParentLink parent, Pageable pageable) {
        Query query = Query.query(GridFsCriteria.whereMetaData("parents").in(parent));
        GridFSFindIterable fsFiles = gridFsAssistant.find(query)
                .skip((int) pageable.getOffset())
                .limit(pageable.getPageSize());
        List<WebFile> files = StreamSupport.stream(switchWebFiles(fsFiles).spliterator(), false)
                .collect(Collectors.toList());
        return new PageImpl<>(files, pageable, gridFsAssistant.count(query));
    }

    /**
     * 根据文件名进行模糊查询
     *
     * @param filename
     * @return
     */
    @Override
    public Stream<WebFile> searchFilesContaining(String filename) {
        Pattern pattern = Pattern.compile("^.*" + filename + ".*$", Pattern.CASE_INSENSITIVE);
        Query query = Query.query(GridFsCriteria.whereFilename().is(pattern));
        return StreamSupport.stream(switchWebFiles(gridFsAssistant.find(query)).spliterator(), false);
    }

    /**
     * 存储文件， 如同名文件已存在则更新文件内容
     *
     * @param file     文件内容输入流
     * @param filename 文件名
     * @param parent   文件类型
     * @return
     */
    @Override
    //@Async
    public ObjectId store(InputStream file, String filename, ParentLink parent) {
        String fileType = FileUtils.getContentType(filename);
        Metadata metadata = new Metadata(fileType,
                new HashSet<ParentLink>() {{
                    add(parent);
                }},
                getParentAuthorizations(parent),
                Collections.emptySet());
        return gridFsAssistant.store(file, filename, metadata);
    }

    private Set<Authorization> getParentAuthorizations(ParentLink parent) {
        if (parent.getTarget().equals(AuthTarget.FOLDER)) {
            Optional<SysFolder> optionalSysFolder = sysFolderRepository.findById(parent.getId());
            if (optionalSysFolder.isPresent()) {
                return optionalSysFolder.get().getAuthorizations();
            }
        } else if (parent.getTarget().equals(AuthTarget.DISK)) {
            Optional<SysDisk> optionalSysDisk = sysDiskRepository.findById(parent.getId());
            if (optionalSysDisk.isPresent()) {
                return optionalSysDisk.get().getAuthorizations();
            }
        }
        return null;
    }

    @Override
    public ObjectId storeServerFile(String resourceLocation) throws IOException {
        Resource resource = new ClassPathResource(resourceLocation);
        String fileType = FileUtils.getMimeType(resource.getFilename());
        Metadata metadata = new Metadata(fileType, Collections.emptySet(),
                new HashSet<Authorization>() {{
                    add(new Authorization("users", AuthType.GROUP));
                }},
                Collections.emptySet());
        return gridFsAssistant.store(resource.getInputStream(), resource.getFilename(), metadata);
    }


    public void rename(ObjectId fileId, String newFilename) {
        gridFsAssistant.rename(fileId, newFilename);
    }

    @Override
    public GridFSFile saveMetadata(ObjectId fileId, Metadata metadata) {
        return gridFsAssistant.updateMetadata(fileId, metadata);
    }

    @Override
    @Async
    public void updateMIMEType(ObjectId fileId){
        GridFSFile file = gridFsAssistant.findOne(Query.query(Criteria.where("_id").is(fileId)));
        if(file != null) {
            Metadata metadata = converter.read(Metadata.class, file.getMetadata());
            try {
                metadata.set_contentType(FileUtils.getMimeType(getResource(fileId).getInputStream(),
                        file.getFilename()));
                gridFsAssistant.updateMetadata(fileId, metadata);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public GridFSFile addParent(ObjectId fileId, ParentLink parent) {
        GridFSFile file = gridFsAssistant.findOne(Query.query(Criteria.where("_id").is(fileId)));
        Metadata metadata = converter.read(Metadata.class, file.getMetadata());
        metadata.getParents().add(parent);
        return gridFsAssistant.updateMetadata(fileId, metadata);
    }

    @Override
    public GridFSFile addAuthorization(ObjectId fileId, Authorization authorization) {
        GridFSFile file = gridFsAssistant.findOne(Query.query(Criteria.where("_id").is(fileId)));
        Metadata metadata = converter.read(Metadata.class, file.getMetadata());
        for (Authorization item : metadata.getAuthorizations()) {
            if (item.getName().equalsIgnoreCase(authorization.getName())
                    && item.getType().equals(authorization.getType())) {
                for (AuthAction action : authorization.getActions()) {
                    item.add(action);
                }
                return gridFsAssistant.updateMetadata(fileId, metadata);
            }
        }
        metadata.addAuthorization(authorization);
        return gridFsAssistant.updateMetadata(fileId, metadata);
    }

    /**
     * 删除Mongo库内文件
     *
     * @param fileId 文件ID
     * @return
     */
    public void delete(ObjectId fileId) {
        gridFsAssistant.delete(new Query(Criteria.where("_id").is(fileId)));
    }

    /**
     * 根据输入文件ID获取二进制流
     *
     * @param fileId 文件ID
     * @return
     */
    @Override
    public GridFsResource getResource(ObjectId fileId) {
        return gridFsAssistant.getResource(fileId);
    }

    @Override
    public GridFSDownloadStream getFSDownloadStream(ObjectId fileId) {
        return gridFsAssistant.getFSDownloadStream(fileId);
    }

    /**
     * 创建ZIP文件
     *
     * @param folderId 文件或文件夹路径
     * @param fos      生成的zip文件存在路径（包括文件名）
     */
    public void createZip(ObjectId folderId, OutputStream fos) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(fos);
        Optional<SysFolder> folder = sysFolderRepository.findById(folderId);
        if (folder.isPresent()) {
            compressFolder(folder.get(), zos, folder.get().getName());
        } else {
            throw new FileNotFoundException("文件夹ID：" + folderId);
        }
        zos.close();
    }

    public void createZipFromList(ObjectId[] ids, OutputStream fos, String parent) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (ObjectId id : ids) {
            compressFile(getResource(id), zos, parent);
        }
        zos.close();
    }

    /**
     * 压缩文件夹
     *
     * @param folder  文件夹实体（含路径ID）
     * @param out     ZIP输出流
     * @param basedir 当前目录
     */
    private void compressFolder(SysFolder folder,
                                ZipOutputStream out,
                                String basedir) {
        // 递归压缩目录
        ParentLink parent = new ParentLink(folder.getId(), AuthTarget.FOLDER, folder.getParent().getDiskId());
        List<SysFolder> folders = sysFolderRepository.findAllByParent(parent);
        if (folders != null && folders.size() > 0) {
            for (SysFolder subFolder : folders) {
                compressFolder(subFolder, out, basedir + "/" + subFolder.getName());
            }
        }
        // 压缩目录内的文件
        switchWebFiles(getStoredFiles(folder.getId()))
                .forEach(file -> {
                    compressFile(getResource(new ObjectId(file.getId())), out, basedir);
                });

    }

    /**
     * 压缩一个文件
     *
     * @param gridFsResource 输入文件流
     * @param out            输出ZIP流
     * @param basedir        当前文件所在目录
     */
    private void compressFile(GridFsResource gridFsResource, ZipOutputStream out, String basedir) {
        try {
            BufferedInputStream bis = new BufferedInputStream(gridFsResource.getInputStream());
            ZipEntry entry = new ZipEntry(basedir + "/" + gridFsResource.getFilename());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER_SIZE];
            while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            bis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * MongoDB存储格式转换为WEB显示格式的文件
     *
     * @param gridFSFile GridFSFile格式文件
     * @return Web格式文件
     */
    private WebFile switchWebFile(GridFSFile gridFSFile) {
        if (gridFSFile == null) return null;
        Metadata metadata = converter.read(Metadata.class, gridFSFile.getMetadata());
        return new WebFile(gridFSFile.getObjectId().toString(),
                gridFSFile.getFilename(),
                gridFSFile.getLength(),
                gridFSFile.getUploadDate(),
                metadata.get_contentType(),
                FileUtils.getIconClass(metadata.get_contentType()),
                false,
                false
        );
    }

    /**
     * 批量转换文件格式
     *
     * @param gridFSFindIterable
     * @return
     */
    private Iterable<WebFile> switchWebFiles(GridFSFindIterable gridFSFindIterable) {
        return gridFSFindIterable.map(gridFSFile -> switchWebFile(gridFSFile));
    }
}
