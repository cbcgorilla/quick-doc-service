package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.common.utils.MessageUtil;
import cn.mxleader.quickdoc.entities.FsCategory;
import cn.mxleader.quickdoc.dao.ReactiveCategoryRepository;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static cn.mxleader.quickdoc.common.utils.KeyUtil.longID;

@Service
public class ReactiveCategoryServiceImpl implements ReactiveCategoryService {

    private final ReactiveCategoryRepository reactiveCategoryRepository;

    ReactiveCategoryServiceImpl(ReactiveCategoryRepository reactiveCategoryRepository) {
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
                .defaultIfEmpty(new FsCategory(longID(), type))
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
                .switchIfEmpty(MessageUtil.noCategoryMsg(oldType))
                .flatMap(category -> {
                    if (reactiveCategoryRepository.findByType(newType)
                            .blockOptional().isPresent()) {
                        return MessageUtil.categoryConflictMsg(newType);
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
                .switchIfEmpty(MessageUtil.noCategoryMsg(type))
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
     *
     * @return
     */
    public Flux<FsCategory> findAll() {
        return reactiveCategoryRepository.findAll();
    }

}
