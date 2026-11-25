import { Component } from '@angular/core';

@Component({
  selector: 'app-programmes-page',
  styleUrl: './programmes.scss',
  templateUrl: './programmes.html',
})
export class ProgrammesPage {
  protected readonly programmeDataAvailable = false;
  protected readonly roleContextAvailable = false;
}
