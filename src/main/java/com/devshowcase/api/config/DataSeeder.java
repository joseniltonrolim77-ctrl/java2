package com.devshowcase.api.config;

import com.devshowcase.api.entity.Feedback;
import com.devshowcase.api.entity.Profile;
import com.devshowcase.api.entity.Project;
import com.devshowcase.api.entity.Technology;
import com.devshowcase.api.repository.FeedbackRepository;
import com.devshowcase.api.repository.ProfileRepository;
import com.devshowcase.api.repository.ProjectRepository;
import com.devshowcase.api.repository.TechnologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Popula o banco com alguns dados de exemplo (perfis, tecnologias, projetos e feedbacks)
 * na primeira inicialização, apenas quando o banco está vazio — assim a API em produção não
 * fica sem nenhum dado para demonstrar/testar logo após o deploy, mas o seed nunca roda de
 * novo (nem duplica dados) em inicializações subsequentes, já que os dados de verdade
 * cadastrados via API passam a existir no banco.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    @Transactional
    CommandLineRunner seedInitialData(
            ProfileRepository profileRepository,
            TechnologyRepository technologyRepository,
            ProjectRepository projectRepository,
            FeedbackRepository feedbackRepository) {
        return args -> {
            if (profileRepository.count() > 0) {
                return;
            }

            log.info("Banco de dados vazio: cadastrando dados de exemplo...");

            Profile ana = new Profile();
            ana.setName("Ana Souza");
            ana.setEmail("ana@example.com");
            ana.setBio("Desenvolvedora backend apaixonada por APIs bem desenhadas.");
            ana.setAvatarUrl("https://i.pravatar.cc/150?u=ana@example.com");
            profileRepository.save(ana);

            Profile carlos = new Profile();
            carlos.setName("Carlos Silva");
            carlos.setEmail("carlos@example.com");
            carlos.setBio("Full-stack, curte Java e React.");
            carlos.setAvatarUrl("https://i.pravatar.cc/150?u=carlos@example.com");
            profileRepository.save(carlos);

            Technology nodejs = technologyRepository.save(newTechnology("Node.js"));
            Technology java = technologyRepository.save(newTechnology("Java"));
            Technology spring = technologyRepository.save(newTechnology("Spring Boot"));
            Technology react = technologyRepository.save(newTechnology("React"));
            Technology postgres = technologyRepository.save(newTechnology("PostgreSQL"));

            Project devshowcase = new Project();
            devshowcase.setTitle("DevShowcase API");
            devshowcase.setDescription("Backend REST da plataforma DevShowcase, com perfis, projetos, tecnologias e feedbacks.");
            devshowcase.setRepositoryUrl("https://github.com/mourapontes/atv2_java_backend");
            devshowcase.setProfile(ana);
            devshowcase.setTechnologies(Set.of(java, spring, postgres));
            projectRepository.save(devshowcase);

            Project portfolio = new Project();
            portfolio.setTitle("Portfólio Pessoal");
            portfolio.setDescription("Site de portfólio com projetos, artigos e formulário de contato.");
            portfolio.setRepositoryUrl("https://github.com/mourapontes/portfolio");
            portfolio.setProfile(carlos);
            portfolio.setTechnologies(Set.of(react, nodejs));
            projectRepository.save(portfolio);

            feedbackRepository.saveAll(List.of(
                    newFeedback(devshowcase, 5, "Excelente projeto, API muito bem documentada!"),
                    newFeedback(devshowcase, 4, "Gostei bastante, só senti falta de autenticação."),
                    newFeedback(portfolio, 5, "Design limpo e responsivo, parabéns!")
            ));

            for (Project project : List.of(devshowcase, portfolio)) {
                Double average = feedbackRepository.findAverageRatingByProjectId(project.getId());
                project.setAverageRating(average != null ? average : 0.0);
                project.setUpvotes(project == devshowcase ? 3 : 1);
                projectRepository.save(project);
            }

            log.info("Dados de exemplo cadastrados com sucesso.");
        };
    }

    private static Technology newTechnology(String name) {
        Technology technology = new Technology();
        technology.setName(name);
        return technology;
    }

    private static Feedback newFeedback(Project project, int rating, String comment) {
        Feedback feedback = new Feedback();
        feedback.setProject(project);
        feedback.setRating(rating);
        feedback.setComment(comment);
        return feedback;
    }
}
