package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.ApplicationSetting;

public interface ApplicationSettingRepository extends JpaRepository<ApplicationSetting, String> {
}
