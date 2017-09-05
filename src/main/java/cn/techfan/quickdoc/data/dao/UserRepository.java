package cn.techfan.quickdoc.data.dao;

import cn.techfan.quickdoc.common.entities.WebUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<WebUser, String> {

    WebUser findByUsername(String type);

}
