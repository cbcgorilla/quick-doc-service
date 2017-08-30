package com.neofinance.quickdoc.service;

import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.common.utils.KeyUtil;
import com.neofinance.quickdoc.repository.CategoryRepository;
import com.neofinance.quickdoc.repository.ReactiveCategoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.neofinance.quickdoc.common.utils.ReactiveErrorMessage.categoryConflictMsg;
import static com.neofinance.quickdoc.common.utils.ReactiveErrorMessage.noCategoryMsg;

@Service
public class ReactiveCategoryService {

    private final CategoryRepository categoryRepository;
    private final ReactiveCategoryRepository reactiveCategoryRepository;

    ReactiveCategoryService(CategoryRepository categoryRepository,
                            ReactiveCategoryRepository reactiveCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.reactiveCategoryRepository = reactiveCategoryRepository;
    }

    /**
     * 新增文件分类
     *
     * @param type
     * @return
     */
    public Mono<FsCategory> addCategory(String type) {
        return reactiveCategoryRepository.findByType(type)
                .defaultIfEmpty(new FsCategory(KeyUtil.longID(), type))
                .flatMap(reactiveCategoryRepository::save);
    }

    /**
     * 重命名文件分类
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param oldType
     * @param newType
     * @return
     */
    public Mono<FsCategory> renameCategory(String oldType, String newType) {
        return reactiveCategoryRepository.findByType(oldType)
                .switchIfEmpty(noCategoryMsg(oldType))
                .flatMap(category -> {
                    if (categoryRepository.findByType(newType) != null) {
                        return categoryConflictMsg(newType);
                    }
                    category.setType(newType);
                    return reactiveCategoryRepository.save(category);
                });
    }

    /**
     * 删除文件分类
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param type
     * @return
     */
    public Mono<Void> deleteCategory(String type) {
        return reactiveCategoryRepository.findByType(type)
                .switchIfEmpty(noCategoryMsg(type))
                .flatMap(reactiveCategoryRepository::delete);
    }

    /**
     * 根据ID获取文件分类信息
     *
     * @param id
     * @return
     */
    public Mono<FsCategory> findById(Long id) {
        return reactiveCategoryRepository.findById(id);
    }

    /**
     * 根据分类名获取FsCategory
     *
     * @param type
     * @return
     */
    public Mono<FsCategory> findByType(String type) {
        return reactiveCategoryRepository.findByType(type);
    }

    /**
     * 获取所有文件分类信息
     * @return
     */
    public Flux<FsCategory> findAll() {
        return reactiveCategoryRepository.findAll();
    }

}
