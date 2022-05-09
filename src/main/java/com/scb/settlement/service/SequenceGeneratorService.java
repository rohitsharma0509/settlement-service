package com.scb.settlement.service;

import com.scb.settlement.model.document.DatabaseSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SequenceGeneratorService {

    private MongoOperations mongoOperations;

    private static final long SEQ_START_WITH = 1;
    private static final long INCREMENT_BY = 1;

    @Autowired
    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public String generateSequence(String seqName) {

        Query query = new Query(Criteria.where("_id").is(seqName));

        Update update = new Update();
        update.inc("seq", INCREMENT_BY);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        DatabaseSequence databaseSequence = mongoOperations.findAndModify(query, update, options,
                DatabaseSequence.class);

        if (Objects.isNull(databaseSequence)) {
            databaseSequence = mongoOperations.save(new DatabaseSequence(seqName, SEQ_START_WITH));
        }

        return String.format("%09d",databaseSequence.getSeq());

    }
}
