<?php

use App\Http\Controllers\Api\MantanTerindahController;
use Illuminate\Support\Facades\Route;

Route::apiResource('mantan', MantanTerindahController::class)->names('api.mantan');
