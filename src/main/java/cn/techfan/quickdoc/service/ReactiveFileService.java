package cn.techfan.quickdoc.service;

import cn.techfan.quickdoc.common.entities.FsDirectory;
import cn.techfan.quickdoc.common.entities.FsEntity;
import cn.techfan.quickdoc.persistent.GridFsAssistant;
import cn.techfan.quickdoc.persistent.dao.CategoryRepository;
import cn.techfan.quickdoc.persistent.dao.DirectoryRepository;
import cn.techfan.quickdoc.persistent.dao.FileEntityRepository;
import cn.techfan.quickdoc.persistent.dao.ReactiveFileEntityRepository;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.techfan.quickdoc.common.query.QueryKit.keyQuery;
import static cn.techfan.quickdoc.common.utils.MessageUtil.fileNotExistMsg;

@Service
public class ReactiveFileService {

    private static final int BUFFER = 8192;

    private final GridFsAssistant gridFsAssistant;
    private final GridFsTemplate gridFsTemplate;
    private final CategoryRepository categoryRepository;
    private final DirectoryRepository directoryRepository;
    private final FileEntityRepository fileEntityRepository;
    private final ReactiveFileEntityRepository reactiveFileEntityRepository;

    ReactiveFileService(GridFsAssistant gridFsAssistant,
                        GridFsTemplate gridFsTemplate,
                        CategoryRepository categoryRepository,
                        DirectoryRepository directoryRepository,
                        FileEntityRepository fileEntityRepository,
                        ReactiveFileEntityRepository reactiveFileEntityRepository) {
        this.gridFsAssistant = gridFsAssistant;
        this.gridFsTemplate = gridFsTemplate;
        this.categoryRepository = categoryRepository;
        this.directoryRepository = directoryRepository;
        this.fileEntityRepository = fileEntityRepository;
        this.reactiveFileEntityRepository = reactiveFileEntityRepository;
    }

    /**
     * 在指定目录内查找相应名字的文件
     *
     * @param filename    输入文件名
     * @param directoryId 所在目录ID
     * @return
     */
    public Mono<FsEntity> getStoredFile(String filename,
                                        Long directoryId) {
        return reactiveFileEntityRepository.findByFilenameAndDirectoryId(filename, directoryId);
    }

    /**
     * 枚举目录内的所有文件
     *
     * @param directoryId 所在目录ID
     * @return
     */
    public Flux<FsEntity> getStoredFiles(Long directoryId) {
        return reactiveFileEntityRepository.findAllByDirectoryId(directoryId)
                .map(v -> {
                    v.setCategory(categoryRepository.findById(v.getCategoryId()).get().getType());
                    v.setDirectory((directoryRepository.findById(v.getDirectoryId()).get().getPath()));
                    return v;
                });
    }

    /**
     * 存储文件， 如同名文件已存在则更新文件内容
     *
     * @param fileEntity 文件描述信息
     * @param file       文件二进制流
     * @return
     */
    public Mono<FsEntity> storeFile(FsEntity fileEntity,
                                    InputStream file) {
        return getStoredFile(fileEntity.getFilename(), fileEntity.getDirectoryId())
                .defaultIfEmpty(fileEntity)
                .flatMap(
                        entity -> {
                            // 删除库中同名历史文件
                            deleteFile(entity);
                            fileEntity.setId(entity.getId());
                            fileEntity.setStoredId(
                                    gridFsTemplate.store(file,
                                            fileEntity.getFilename(),
                                            fileEntity.getContentType()
                                    )
                            );
                            return reactiveFileEntityRepository.save(fileEntity);
                        }
                );
    }

    /**
     * 删除Mongo库内文件
     *
     * @param fileEntity 文件信息
     * @return
     */
    public Mono<Void> deleteFile(FsEntity fileEntity) {
        return getStoredFile(fileEntity.getFilename(), fileEntity.getDirectoryId())
                .switchIfEmpty(
                        fileNotExistMsg(new Long(fileEntity.getDirectoryId()).toString(),
                                fileEntity.getFilename())
                )
                .flatMap(entity -> {
                    gridFsTemplate.delete(keyQuery(entity.getStoredId()));
                    return reactiveFileEntityRepository.delete(entity);
                });
    }

    /**
     * 删除Mongo库内文件
     *
     * @param fsEntityId 文件ID信息
     * @return
     */
    public Mono<Void> deleteFile(String fsEntityId) {
        return reactiveFileEntityRepository.findById(fsEntityId)
                .switchIfEmpty(
                        fileNotExistMsg(fsEntityId)
                )
                .flatMap(entity -> {
                    gridFsTemplate.delete(keyQuery(entity.getStoredId()));
                    return reactiveFileEntityRepository.delete(entity);
                });
    }

    /**
     * 根据输入文件ID获取二进制流
     *
     * @param storedId 文件ID
     * @return
     */
    public GridFSDownloadStream getFileStream(ObjectId storedId) {
        return gridFsAssistant.getResource(storedId);
    }

    /**
     * 创建ZIP文件
     *
     * @param directoryId 文件或文件夹路径
     * @param fos         生成的zip文件存在路径（包括文件名）
     * @param categoryId  待压缩的文件分类ID，为0L则压缩所有分类
     */
    public void createZip(Long directoryId,
                          OutputStream fos,
                          Long categoryId) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(fos);
        Optional<FsDirectory> optionalDirectory = directoryRepository.findById(directoryId);
        if (optionalDirectory.isPresent()) {
            FsDirectory directory = optionalDirectory.get();
            compressDirectory(directory, zos, directory.getPath(), categoryId);
        } else {
            throw new FileNotFoundException("文件夹ID：" + directoryId);
        }
        zos.close();
    }

    /**
     * 压缩文件夹
     *
     * @param directory  文件夹实体（含路径ID）
     * @param out        ZIP输出流
     * @param basedir    当前目录
     * @param categoryId 待压缩的文件分类ID，为0L则压缩所有分类
     */
    private void compressDirectory(FsDirectory directory,
                                   ZipOutputStream out,
                                   String basedir,
                                   Long categoryId) {
        // 递归压缩目录
        List<FsDirectory> directories = directoryRepository.findAllByParentId(directory.getId());
        if (directories != null && directories.size() > 0) {
            for (FsDirectory subdirectory : directories) {
                compressDirectory(subdirectory,
                        out,
                        basedir + "/" + subdirectory.getPath(),
                        categoryId);
            }
        }
        // 压缩目录内的文件
        List<FsEntity> fileList = null;
        if (categoryId == 0) {
            fileList = fileEntityRepository.findAllByDirectoryId(directory.getId());
        } else {
            fileList = fileEntityRepository.findAllByDirectoryIdAndCategoryId(directory.getId(), categoryId);
        }
        if (fileList != null && fileList.size() > 0) {
            for (FsEntity file : fileList) {
                compressFile(getFileStream(file.getStoredId()), out, basedir);
            }
        }
    }

    /**
     * 压缩一个文件
     *
     * @param fsInputStream 输入文件流
     * @param out           输出ZIP流
     * @param basedir       当前文件所在目录
     */
    private void compressFile(GridFSDownloadStream fsInputStream,
                              ZipOutputStream out,
                              String basedir) {
        try {
            BufferedInputStream bis = new BufferedInputStream(fsInputStream);
            ZipEntry entry = new ZipEntry(basedir + "/" + fsInputStream.getGridFSFile().getFilename());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            bis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
