package com.ouroboros.notes.adapter.out.persistence;

import com.ouroboros.notes.domain.Note;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

/** Implementacao do fragmento de busca via {@link MongoTemplate}. */
public class NoteSearchRepositoryImpl implements NoteSearchRepository {

  private final MongoTemplate mongoTemplate;

  public NoteSearchRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<Note> search(String userId, String tag, String text) {
    return mongoTemplate.find(buildQuery(userId, tag, text), Note.class);
  }

  /**
   * Monta a query escopada pelo usuario, somando os filtros presentes (tag e/ou texto) e ordenando
   * pela data de modificacao desc. Extraido como metodo estatico para teste de unidade sem banco.
   */
  static Query buildQuery(String userId, String tag, String text) {
    List<Criteria> parts = new ArrayList<>();
    parts.add(Criteria.where("userId").is(userId));
    if (StringUtils.hasText(tag)) {
      parts.add(Criteria.where("tags").is(tag));
    }
    if (StringUtils.hasText(text)) {
      String regex = Pattern.quote(text);
      parts.add(
          new Criteria()
              .orOperator(
                  Criteria.where("title").regex(regex, "i"),
                  Criteria.where("content").regex(regex, "i")));
    }
    Criteria all = new Criteria().andOperator(parts.toArray(Criteria[]::new));
    return new Query(all).with(Sort.by(Sort.Direction.DESC, "updatedAt"));
  }
}
