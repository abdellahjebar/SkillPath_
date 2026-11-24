import { Component, computed, signal } from '@angular/core';

export type SampleSizeStatus = 'insufficient' | 'caution' | null;

export function getSampleSizeStatus(effectif: number | null): SampleSizeStatus {
  if (effectif === null || effectif >= 93) return null;
  if (effectif < 39) return 'insufficient';
  return 'caution';
}

@Component({
  selector: 'app-dashboard-page',
  styleUrl: './dashboard.scss',
  templateUrl: './dashboard.html',
})
export class DashboardPage {
  readonly selectedDomain = signal('M18');
  readonly selectedLocation = signal('France');
  readonly selectedPeriod = signal('');

  /** Set from the real segment response when the analytics API is connected. */
  readonly effectif = signal<number | null>(null);
  readonly sampleSizeStatus = computed(() => getSampleSizeStatus(this.effectif()));

  updateDomain(event: Event): void {
    if (event.target instanceof HTMLSelectElement) {
      this.selectedDomain.set(event.target.value);
    }
  }

  updateLocation(event: Event): void {
    if (event.target instanceof HTMLSelectElement) {
      this.selectedLocation.set(event.target.value);
    }
  }

  updatePeriod(event: Event): void {
    if (event.target instanceof HTMLInputElement) {
      this.selectedPeriod.set(event.target.value);
    }
  }
}
