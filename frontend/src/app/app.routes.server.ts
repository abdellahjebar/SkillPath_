import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'competences/:competenceId',
    renderMode: RenderMode.Server,
  },
  {
    path: 'modules/:moduleId/correction',
    renderMode: RenderMode.Server,
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
