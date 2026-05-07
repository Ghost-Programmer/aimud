import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-server-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './server-settings.component.html',
  styleUrl: './server-settings.component.css'
})
export class ServerSettingsComponent implements OnInit {
  serverSettingsForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService,
    private titleService: Title
  ) {
    this.serverSettingsForm = this.fb.group({
      id: [null],
      serverName: ['', Validators.required],
      allowNewUser: [true],
      maintenance: [false],
      maintenanceText: [''],
      mudHour: [0, [Validators.required, Validators.min(0), Validators.max(23)]],
      mudDay: [1, [Validators.required, Validators.min(1), Validators.max(28)]],
      mudMonth: [1, [Validators.required, Validators.min(1), Validators.max(13)]],
      mudYear: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit() {
    this.loadServerSettings();
  }

  loadServerSettings() {
    this.configService.getServerSettings().subscribe(settings => {
      this.serverSettingsForm.patchValue(settings);
    });
  }

  saveServerSettings() {
    if (this.serverSettingsForm.valid) {
      this.configService.updateServerSettings(this.serverSettingsForm.value).subscribe(() => {
        const serverName = this.serverSettingsForm.get('serverName')?.value;
        this.titleService.setTitle(serverName || 'AI Mud');
        alert('Server settings updated');
      });
    }
  }
}
