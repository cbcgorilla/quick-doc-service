package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.FsCategory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveCategoryService {

    /**
     * 新增文件分类
     *
     * @param type
     * @return
     */
    Mono<FsCategory> addCategory(String type);

    /**
     * 重命名文件分类
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param oldType
     * @param newType
     * @return
     */
    Mono<FsCategory> renameCategory(String oldType, String newType);

    /**
     * 删除文件分类
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param type
     * @return
     */
    Mono<Void> deleteCategory(String type);

    /**
     * 根据ID获取文件分类信息
     *
     * @param id
     * @return
     */
    Mono<FsCategory> findById(Long id);

    /**
     * 根据分类名获取FsCategory
     *
     * @param type
     * @return
     */
    Mono<FsCategory> findByType(String type);

    /**
     * 获取所有文件分类信息
     *
     * @return
     */
    Flux<FsCategory> findAll();

}
