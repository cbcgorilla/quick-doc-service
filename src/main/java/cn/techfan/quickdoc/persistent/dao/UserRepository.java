package cn.techfan.quickdoc.persistent.dao;

import cn.techfan.quickdoc.entities.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserEntity, String> {

    UserEntity findByUsername(String type);

}
