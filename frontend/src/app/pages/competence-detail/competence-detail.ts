import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';

@Component({
  selector: 'app-competence-detail-page',
  imports: [RouterLink],
  styleUrls: ['./competence-detail.scss'],
  templateUrl: './competence-detail.html',
})
export class CompetenceDetailPage {
  private readonly route = inject(ActivatedRoute);

  readonly competenceId = toSignal(
    this.route.paramMap.pipe(map((params) => params.get('competenceId'))),
    { initialValue: null },
  );
}
