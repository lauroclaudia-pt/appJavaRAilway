package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.Attachment;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByResModelAndResId(String resModel, Long resId);
    List<Attachment> findByResModelAndResIdAndPublicDocumentTrue(String resModel, Long resId);
}
