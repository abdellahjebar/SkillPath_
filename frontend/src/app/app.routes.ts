import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { MainLayout } from './layout/main-layout';
import { DashboardPage } from './pages/dashboard/dashboard';
import { CompetenceDetailPage } from './pages/competence-detail/competence-detail';
import { ProgrammesPage } from './pages/programmes/programmes';
import { AdministrationPage } from './pages/administration/administration';
import { ModuleCorrectionPage } from './pages/module-correction/module-correction';

export const routes: Routes = [
  { path: '', redirectTo: 'connexion', pathMatch: 'full' },
  { path: 'connexion', component: Login },
  {
    path: '',
    component: MainLayout,
    children: [
      { path: 'tableau-de-bord', component: DashboardPage },
      { path: 'competences', component: CompetenceDetailPage },
      { path: 'competences/:competenceId', component: CompetenceDetailPage },
      { path: 'programmes', component: ProgrammesPage },
      { path: 'administration', component: AdministrationPage },
      { path: 'modules/correction', component: ModuleCorrectionPage },
      { path: 'modules/:moduleId/correction', component: ModuleCorrectionPage },
    ],
  },
];
